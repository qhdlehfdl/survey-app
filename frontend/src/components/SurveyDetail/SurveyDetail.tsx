// src/components/SurveyDetail/SurveyDetail.tsx
import React, { useEffect, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import styles from "./SurveyDetail.module.css";
import { useAuth } from "../../auth/AuthContext";
import getSurveyDetail from "../../api/getSurveyDetail";

type Option = {
  id: number;
  text: string;
  order?: number;
};

type Question = {
  id: number;
  questionId: string;
  text: string;
  type: "ANSWER" | "MULTIPLE" | "CHECKBOX" | "DROPDOWN" | string;
  required: boolean;
  order?: number;
  options: Option[];
};

type SurveyPayload = {
  id: number;
  title: string;
  writerId?: number;
  questions: Question[];
  participantsNum?: number;
};

export default function SurveyDetail() {
  const { id } = useParams<{ id: string }>();
  const surveyId = id ? parseInt(id, 10) : NaN;
  const navigate = useNavigate();
  const { isAuthenticated } = useAuth();

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [survey, setSurvey] = useState<SurveyPayload | null>(null);

  // answers: ANSWER -> string, MULTIPLE/DROPDOWN -> number, CHECKBOX -> number[]
  const [answers, setAnswers] = useState<
    Record<string, string | number | number[]>
  >({});

  useEffect(() => {
    if (isNaN(surveyId)) return;

    let aborted = false;
    const controller = new AbortController();

    async function load() {
      setLoading(true);
      setError(null);

      try {
        const data = await getSurveyDetail(surveyId, controller.signal);

        // data might be { survey: { ... } } or direct survey payload
        const raw: any = data?.survey ?? data;

        // ensure participantsNum exists and is a number
        const candidate = raw?.participantsNum;
        if (candidate != null && typeof candidate === "number") {
          raw.participantsNum = candidate;
        } else {
          raw.participantsNum = raw.participantsNum ?? 0;
        }

        const payload: SurveyPayload | undefined = raw;
        if (!payload || !payload.questions) {
          throw new Error("설문 데이터를 불러오지 못했습니다.");
        }

        if (aborted) return;

        setSurvey(payload);

        // initialize answers
        const init: Record<string, string | number | number[]> = {};
        payload.questions.forEach((q) => {
          if (q.type === "CHECKBOX") init[q.questionId] = [];
          else init[q.questionId] = "";
        });
        setAnswers(init);
      } catch (err: any) {
        if (err.name === "AbortError") return;
        console.error(err);
        setError(err.message ?? "설문을 불러오는 중 오류가 발생했습니다.");
      } finally {
        if (!aborted) setLoading(false);
      }
    }

    load();

    return () => {
      aborted = true;
      controller.abort();
    };
  }, [surveyId]);

  if (isNaN(surveyId)) return <div>잘못된 설문 아이디입니다.</div>;

  function handleAnswerChange(
    question: Question,
    value: string | number | number[]
  ) {
    setAnswers((prev) => ({ ...prev, [question.questionId]: value }));
  }

  function handleCheckboxToggle(question: Question, optionId: number) {
    const key = question.questionId;
    setAnswers((prev) => {
      const cur = (prev[key] as number[]) ?? [];
      const set = new Set<number>(cur);
      if (set.has(optionId)) set.delete(optionId);
      else set.add(optionId);
      return { ...prev, [key]: Array.from(set) };
    });
  }

  function validate(): { ok: boolean; message?: string } {
    if (!survey) return { ok: false, message: "설문이 로드되지 않았습니다." };
    for (const q of survey.questions) {
      if (!q.required) continue;
      const val = answers[q.questionId];
      if (q.type === "CHECKBOX") {
        if (!Array.isArray(val) || (val as number[]).length === 0) {
          return { ok: false, message: `필수 질문 "${q.text}"에 답해주세요.` };
        }
      } else {
        if (val === null || val === undefined) {
          return { ok: false, message: `필수 질문 "${q.text}"에 답해주세요.` };
        }
        if (typeof val === "string") {
          if (val.trim() === "")
            return {
              ok: false,
              message: `필수 질문 "${q.text}"에 답해주세요.`,
            };
        }
      }
    }
    return { ok: true };
  }

  async function handleSubmit(e: React.FormEvent) {
    e.preventDefault();

    // === 여기서 로그인 검사 ===
    if (!isAuthenticated) {
      alert("로그인 후 이용 가능합니다");
      navigate("/sign-in");
      return;
    }

    const v = validate();
    if (!v.ok) {
      alert(v.message);
      return;
    }

    // build payloadAnswers same as before
    const payloadAnswers = survey!.questions.map((q) => {
      const raw = answers[q.questionId];

      if (q.type === "CHECKBOX") {
        if (Array.isArray(raw) && (raw as number[]).length > 0)
          return { questionId: q.questionId, answer: raw };
        else return { questionId: q.questionId, answer: null };
      }

      if (raw === null || raw === undefined)
        return { questionId: q.questionId, answer: null };
      if (typeof raw === "string") {
        const trimmed = raw.trim();
        return {
          questionId: q.questionId,
          answer: trimmed === "" ? null : trimmed,
        };
      }
      if (typeof raw === "number")
        return { questionId: q.questionId, answer: raw };
      return { questionId: q.questionId, answer: null };
    });

    // POST submission logic remains as before (token refresh handled at API util if you choose)
    let didRefresh = false;

    async function doFetch(token: string | null) {
      const headers: Record<string, string> = {
        "Content-Type": "application/json",
        Accept: "application/json",
      };
      if (token) headers["Authorization"] = `Bearer ${token}`;

      const res = await fetch(`/api/survey/${surveyId}/response`, {
        method: "POST",
        headers,
        body: JSON.stringify({ answers: payloadAnswers }),
      });

      let data: any = null;
      try {
        data = await res.json();
      } catch (e) {
        data = null;
      }
      return { res, data };
    }

    try {
      let accessToken = localStorage.getItem("accessToken");
      let { res, data } = await doFetch(accessToken);

      if (data?.code === "NP" && !didRefresh) {
        const { refreshAccessToken } = await import("../../api/refreshToken");
        const newAccess = await refreshAccessToken();
        if (newAccess) {
          localStorage.setItem("accessToken", newAccess);
          didRefresh = true;
          accessToken = newAccess;
          const retry = await doFetch(accessToken);
          res = retry.res;
          data = retry.data;
        } else {
          alert("세션이 만료되었습니다. 다시 로그인해주세요.");
          localStorage.removeItem("accessToken");
          window.location.href = "/sign-in";
          return;
        }
      }

      if (data?.code === "SU") {
        alert("제출되었습니다. 감사합니다.");
        navigate("/main");
      } else if (data?.code === "NP") {
        alert("세션이 만료되었습니다. 다시 로그인해주세요.");
        localStorage.removeItem("accessToken");
        window.location.href = "/sign-in";
      } else if (data?.code === "AA") {
        alert("이미 응답하셨습니다.");
      } else if (data?.code === "EA") {
        alert("필수입력 항목에 답변해야합니다.");
      } else {
        alert(data?.message ?? "제출 중 오류가 발생했습니다.");
      }
    } catch (err: any) {
      console.error(err);
      alert(err.message ?? "제출 중 오류가 발생했습니다.");
    }
  }

  return (
    <div className={styles.container}>
      {loading ? (
        <div>로딩 중...</div>
      ) : error ? (
        <div style={{ color: "red" }}>{error}</div>
      ) : survey ? (
        <>
          <header className={styles.header}>
            <h1>{survey.title}</h1>

            <div className={styles.headerMeta}>
              <div className={styles.meta}>
                작성자: {survey.writerId ?? "알 수 없음"}
              </div>

              <div
                className={styles.participantCount}
                aria-label={`참여자 수 ${survey.participantsNum ?? 0}`}
              >
                <div className={styles.participantLabel}>참여자</div>
                <div className={styles.participantValue}>
                  {survey.participantsNum ?? 0}
                </div>
              </div>
            </div>
          </header>

          <form onSubmit={handleSubmit} className={styles.form}>
            {survey.questions
              .sort((a, b) => (a.order ?? 0) - (b.order ?? 0))
              .map((q) => (
                <div key={q.questionId} className={styles.questionCard}>
                  <label className={styles.questionLabel}>
                    <span className={styles.qText}>
                      {q.order != null ? `${q.order}. ` : ""}
                      {q.text}
                      {q.required && <span style={{ color: "red" }}> *</span>}
                    </span>
                  </label>

                  {q.type === "ANSWER" && (
                    <textarea
                      value={(answers[q.questionId] as string) ?? ""}
                      onChange={(e) => handleAnswerChange(q, e.target.value)}
                      className={styles.textarea}
                      rows={4}
                    />
                  )}

                  {q.type === "MULTIPLE" && (
                    <div>
                      {q.options.map((opt) => (
                        <label key={opt.id} className={styles.optionLabel}>
                          <input
                            type="radio"
                            name={q.questionId}
                            value={String(opt.id)}
                            checked={answers[q.questionId] === opt.id}
                            onChange={() => handleAnswerChange(q, opt.id)}
                          />
                          <span>{opt.text}</span>
                        </label>
                      ))}
                    </div>
                  )}

                  {q.type === "CHECKBOX" && (
                    <div>
                      {q.options.map((opt) => {
                        const selected =
                          (answers[q.questionId] as number[]) ?? [];
                        return (
                          <label key={opt.id} className={styles.optionLabel}>
                            <input
                              type="checkbox"
                              name={`${q.questionId}_${opt.id}`}
                              value={String(opt.id)}
                              checked={selected.includes(opt.id)}
                              onChange={() => handleCheckboxToggle(q, opt.id)}
                            />
                            <span>{opt.text}</span>
                          </label>
                        );
                      })}
                    </div>
                  )}

                  {q.type === "DROPDOWN" && (
                    <select
                      value={
                        answers[q.questionId] === ""
                          ? ""
                          : String(answers[q.questionId])
                      }
                      onChange={(e) =>
                        handleAnswerChange(
                          q,
                          e.target.value === "" ? "" : Number(e.target.value)
                        )
                      }
                      className={styles.select}
                    >
                      <option value="">선택하세요</option>
                      {q.options.map((opt) => (
                        <option key={opt.id} value={String(opt.id)}>
                          {opt.text}
                        </option>
                      ))}
                    </select>
                  )}
                </div>
              ))}

            <div className={styles.actions}>
              <button type="submit" className={styles.submitBtn}>
                제출
              </button>
              <button
                type="button"
                className={styles.cancelBtn}
                onClick={() => navigate("/main")}
              >
                취소
              </button>
            </div>
          </form>
        </>
      ) : (
        <div>설문을 찾을 수 없습니다.</div>
      )}
    </div>
  );
}
