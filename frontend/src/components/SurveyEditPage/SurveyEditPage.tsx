// src/components/SurveyForm/FormBuilder.tsx
import React, { useEffect, useState } from "react";
import { useParams, useNavigate, useLocation } from "react-router-dom";
import { Question as LocalQuestion, Option } from "../SurveyForm/types";
import QuestionBlock from "../SurveyForm/QuestionBlock";
import { v4 as uuidv4 } from "uuid";
import styles from "../SurveyForm/SurveyForm.module.css";
import getSurveyDetail from "../../api/getSurveyDetail";

export default function FormBuilder() {
  const { id } = useParams<{ id?: string }>(); // optional survey id for edit
  const surveyId = id ? parseInt(id, 10) : NaN;
  const navigate = useNavigate();
  const location = useLocation();
  const participantsNum = (location.state as { participantsNum?: Number })
    ?.participantsNum;

  const [title, setTitle] = useState("설문지 제목을 입력하세요");
  const [questions, setQuestions] = useState<LocalQuestion[]>([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);

  const mapTypeFromServer = (t: string | undefined) => {
    if (!t) return "answer";
    switch (t.toUpperCase()) {
      case "ANSWER":
        return "answer";
      case "MULTIPLE":
        return "multiple";
      case "CHECKBOX":
        return "checkbox";
      case "DROPDOWN":
        return "dropdown";
      default:
        return "answer";
    }
  };

  const mapTypeToServer = (local: string) => {
    switch ((local || "").toLowerCase()) {
      case "answer":
        return "ANSWER";
      case "multiple":
        return "MULTIPLE";
      case "checkbox":
        return "CHECKBOX";
      case "dropdown":
        return "DROPDOWN";
      default:
        return "ANSWER";
    }
  };

  // fetch survey detail when editing an existing survey
  useEffect(() => {
    if (!id) return; // create mode
    if (isNaN(surveyId)) {
      setError("잘못된 설문 아이디입니다.");
      return;
    }

    let mounted = true;
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(null);
      try {
        const data = await getSurveyDetail(surveyId, controller.signal);
        const raw: any = data?.survey ?? data;
        if (!raw) throw new Error("설문을 불러오지 못했습니다.");

        if (mounted && raw.title) setTitle(raw.title);

        const serverQuestions: any[] = raw.questions ?? [];
        const mapped: LocalQuestion[] = serverQuestions.map((sq: any) => {
          const qId = (sq.questionId ?? sq.id ?? uuidv4()).toString();
          const qType = mapTypeFromServer(sq.type);
          const qOptions: Option[] = (sq.options ?? []).map((o: any) => {
            const text = o && o.text ? String(o.text) : String(o);
            return { text, isNew: false };
          });

          return {
            id: qId,
            text: sq.text ?? "",
            type: qType,
            options: qOptions,
            required: !!sq.required,
            isExisting: true,
            participantsNum: Number(sq.participantsNum ?? 0),
          } as LocalQuestion;
        });

        if (mounted) setQuestions(mapped);
      } catch (err: any) {
        if (err.name === "AbortError") return;
        console.error("설문 불러오기 오류:", err);
        if (mounted)
          setError(err.message ?? "설문을 불러오는 중 오류가 발생했습니다.");
      } finally {
        if (mounted) setLoading(false);
      }
    }

    load();

    return () => {
      mounted = false;
      controller.abort();
    };
  }, [id, surveyId]);

  function addQuestion() {
    const q: LocalQuestion = {
      id: uuidv4(),
      text: "",
      type: "answer",
      options: [],
      required: false,
      isExisting: false,
      participantsNum: 0,
    };
    setQuestions((s) => [...s, q]);
  }

  function updateQuestion(id: string, patch: Partial<LocalQuestion>) {
    setQuestions((s) => s.map((q) => (q.id === id ? { ...q, ...patch } : q)));
  }

  function deleteQuestion(id: string) {
    setQuestions((s) => s.filter((q) => q.id !== id));
  }

  // ----- 새로 추가된 부분: 서버 DTO에 맞는 payload 빌더 -----
  // 기존에 있던 helper를 아래로 교체하세요.
  function buildServerPayload(titleStr: string, qList: LocalQuestion[]) {
    const questionsDto = qList.map((q, idx) => {
      // 옵션: Local Option object -> 문자열 배열
      const cleanedOptions = (q.options ?? [])
        .map((opt) => (typeof opt === "string" ? opt : opt.text ?? ""))
        .map((s) => (s == null ? "" : String(s).trim()))
        .filter((s) => s !== ""); // 빈 문자열 옵션은 제거

      const dto: any = {
        // **항상** id를 보냅니다 — 새로 만든 질문도 uuidv4()로 생성된 q.id를 전송
        id: q.id, // <-- 여기! 기존/신규 모두 UUID 문자열로 전송
        text: q.text ?? "",
        type: mapTypeToServer(q.type),
        required: !!q.required,
        options: cleanedOptions,
        order: idx + 1,
      };

      // 서버가 'id' 없는 신규 질문을 기대한다면 이 줄을 제거하세요.
      // (하지만 지금 요청사항은 "신규 질문에도 id UUID 보냄" 이므로 id는 남겨둡니다.)

      return dto;
    });

    return {
      title: titleStr,
      questions: questionsDto,
    };
  }

  async function registerDraft(retry = false): Promise<void> {
    // build payload in server DTO shape
    const payload = buildServerPayload(title, questions);

    try {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken && !retry) {
        alert("로그인이 필요합니다.");
        window.location.href = "/sign-in";
        return;
      }

      // 정해진 엔드포인트: create -> POST /api/survey, edit -> PATCH /api/survey/{surveyId}
      const isEdit = !!id && !isNaN(surveyId);
      const url = isEdit ? `/api/survey/${surveyId}` : "/api/survey";
      const method = isEdit ? "PATCH" : "POST";

      const res = await fetch(url, {
        method,
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`,
        },
        body: JSON.stringify(payload),
      });

      // try parse JSON safely
      let data: any = null;
      try {
        data = await res.json();
      } catch (e) {
        data = null;
      }

      if (!res.ok) {
        // 서버 에러 / validation 실패 등
        const txt =
          data?.message ?? (await res.text().catch(() => res.statusText));
        throw new Error(`서버 응답 오류: ${res.status} ${txt}`);
      }

      if (data?.code === "SU") {
        alert(isEdit ? "설문이 수정되었습니다." : "설문지가 등록되었습니다.");
        // 성공 시 리스트나 상세로 이동
        if (isEdit) navigate(`/profile`);
        else navigate("/"); // 등록 후 루트
        return;
      }

      // 인증 만료 코드 처리: NP -> refresh token flow
      if (data?.code === "NP") {
        if (retry) {
          alert("세션 갱신 실패. 다시 로그인 해주세요.");
          localStorage.removeItem("accessToken");
          window.location.href = "/sign-in";
          return;
        }

        const newAccessToken = await import("../../api/refreshToken").then(
          (m) => m.refreshAccessToken()
        );
        if (!newAccessToken) {
          alert("세션이 만료되었습니다. 다시 로그인해주세요.");
          localStorage.removeItem("accessToken");
          window.location.href = "/sign-in";
          return;
        }

        localStorage.setItem("accessToken", newAccessToken);
        await registerDraft(true);
        return;
      }

      // 기타 서버 반환 처리
      alert(data?.message ?? "서버 응답을 처리하지 못했습니다.");
    } catch (err: any) {
      console.error("서버 오류:", err);
      alert(err.message ?? "서버 오류로 등록 실패");
    }
  }

  return (
    <div className={styles.builderWrap}>
      <div className={styles.builderInner}>
        {loading && <div>로딩 중...</div>}
        {error && <div style={{ color: "red" }}>{error}</div>}

        <div className={styles.formTitle}>
          <input
            type="text"
            value={title}
            onChange={(e) => setTitle(e.target.value)}
            placeholder="설문지 제목을 입력하세요"
          />
        </div>

        {questions.map((q) => (
          <QuestionBlock
            key={q.id}
            q={q}
            onChange={updateQuestion}
            onDelete={deleteQuestion}
          />
        ))}

        <div className={styles.bottomArea}>
          <button className={styles.addBtn} onClick={addQuestion}>
            + 질문 추가
          </button>
          <div className={styles.saveArea}>
            (
            <button
              onClick={() => {
                if (questions.length === 0) {
                  alert("설문지는 최소 1개의 질문을 입력해야합니다.");
                  return;
                }
                registerDraft(false);
              }}
              className={styles.primary}
            >
              등록
            </button>
            )
          </div>
        </div>
      </div>
    </div>
  );
}
