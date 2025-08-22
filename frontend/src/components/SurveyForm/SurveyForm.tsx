import React, { useState } from "react";
import { Question } from "./types";
import QuestionBlock from "./QuestionBlock";
import { v4 as uuidv4 } from "uuid";
import styles from "./SurveyForm.module.css";
import { refreshAccessToken } from "../../api/refreshToken";

export default function FormBuilder() {
  const [title, setTitle] = useState("설문지 제목을 입력하세요");
  const [questions, setQuestions] = useState<Question[]>([]);

  function addQuestion() {
    const q: Question = {
      id: uuidv4(),
      text: "",
      type: "answer",
      options: [],
      required: false,
    };
    setQuestions((s) => [...s, q]);
    // 스크롤을 맨 아래로 보내는 로직을 여기에 추가해도 좋음
  }

  function updateQuestion(id: string, patch: Partial<Question>) {
    setQuestions((s) => s.map((q) => (q.id === id ? { ...q, ...patch } : q)));
  }

  function deleteQuestion(id: string) {
    setQuestions((s) => s.filter((q) => q.id !== id));
  }

  async function registerDraft(retry = false): Promise<void> {
    const payload = { title, questions };

    try {
      const accessToken = localStorage.getItem("accessToken");
      if (!accessToken && !retry) {
        alert("로그인이 필요합니다.");
        window.location.href = "/sign-in";
        return;
      }

      const res = await fetch("/api/survey", {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
          Authorization: `Bearer ${accessToken}`, // Bearer로 access token 전달
        },
        body: JSON.stringify(payload),
      });

      const data = await res.json();

      if (data.code === "SU") {
        console.log("등록 성공:", data);
        alert("설문지가 등록되었습니다.");
        // 필요하다면 등록 후 페이지 이동
        window.location.href = "/";
        return;
      } else if (data.code === "DBE") {
        const errorData = await res.json().catch(() => null);
        console.error("등록 실패:", errorData || res.statusText);
        alert(`등록 실패: ${errorData?.message || res.status}`);
        return;
      } else if (data.code === "NP") {
        if (retry) {
          alert("등록에 실패했습니다. 다시 로그인해주세요.");
          localStorage.removeItem("accessToken");
          window.location.href = "/sign-in";
          return;
        }

        const newAccessToken = await refreshAccessToken();

        if (!newAccessToken) {
          // 갱신 실패 -> 강제 로그인
          alert("등록에 실패했습니다. 다시 로그인해주세요.");
          localStorage.removeItem("accessToken");
          window.location.href = "/sign-in";
          return;
        }

        localStorage.setItem("accessToken", newAccessToken);
        await registerDraft(true);
        return;
      }
    } catch (err) {
      console.error("서버 오류:", err);
      alert("서버 오류로 등록 실패");
    }
  }

  return (
    <div className={styles.builderWrap}>
      <div className={styles.builderInner}>
        {/* 설문지 제목 입력 */}
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
