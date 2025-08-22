import React, { useEffect, useMemo, useState } from "react";
import { useNavigate, useParams } from "react-router-dom";
import getSurveyResult from "../../api/getSurveyResult";
import styles from "./SurveyResultPage.module.css";

type QuestionOption = {
  id: number;
  text: string;
  order: number;
  answersCount?: number; // for option-based questions
};

type QuestionResult = {
  id: number;
  questionId: string;
  text: string;
  type: string; // ANSWER | MULTIPLE | CHECKBOX | DROPDOWN etc.
  required: boolean;
  order: number;
  options: QuestionOption[];
  answers?: string[]; // for ANSWER type
};

type SurveyPayload = {
  survey: {
    id: number;
    title: string;
    writerId: number;
    participantsNum: number;
    questions: QuestionResult[];
  };
  code?: string;
  message?: string;
};

       
export default function SurveyResultPage() {
  const { id } = useParams();
  const navigate = useNavigate();
  const surveyId = Number(id);

  const [loading, setLoading] = useState(false);
  const [error, setError] = useState<string | null>(null);
  const [data, setData] = useState<SurveyPayload | null>(null);

  useEffect(() => {
    if (!surveyId || Number.isNaN(surveyId)) {
      setError("잘못된 설문 ID입니다.");
      return;
    }

    const controller = new AbortController();

    const fetchResult = async () => {
      setLoading(true);
      setError(null);

      try {
        const json = await getSurveyResult(surveyId);
        setData(json);
      } catch (err: any) {
        console.error("fetch survey result error", err);
        setError(
          err?.message || "설문 결과를 불러오는 중 오류가 발생했습니다."
        );
      } finally {
        setLoading(false);
      }
    };

    fetchResult();

    return () => controller.abort();
  }, [surveyId]);

  const maxCountsByQuestion = useMemo(() => {
    if (!data) return new Map<number, number>();
    const map = new Map<number, number>();
    data.survey.questions.forEach((q) => {
      const max =
        q.options && q.options.length > 0
          ? Math.max(...q.options.map((o) => o.answersCount ?? 0))
          : 0;
      map.set(q.id, Math.max(max, 1)); // avoid division by zero
    });
    return map;
  }, [data]);

  const renderOptionBars = (q: QuestionResult) => {
    const max = maxCountsByQuestion.get(q.id) ?? 1;
    return (
      <div className={styles.optionList}>
        {q.options.map((opt) => {
          const count = opt.answersCount ?? 0;
          const pct = Math.round((count / max) * 100);
          return (
            <div key={opt.id} className={styles.optionRow}>
              <div className={styles.optionText}>{opt.text}</div>
              <div className={styles.track}>
                <div className={styles.fill} style={{ width: `${pct}%` }} />
              </div>
              <div className={styles.count}>{count}</div>
            </div>
          );
        })}
      </div>
    );
  };

  return (
    <div className={styles.container}>
      <div className={styles.headerRow}>
        <h1 className={styles.title}>
          {data ? data.survey.title : "설문 결과"}
        </h1>
        <div className={styles.actions}>
          <button className={styles.backButton} onClick={() => navigate(-1)}>
            뒤로
          </button>
        </div>
      </div>

      {loading && <div className={styles.loading}>불러오는 중…</div>}
      {error && <div className={styles.error}>{error}</div>}

      {!loading && !error && data && (
        <section>
          <div className={styles.participants}>
            참여자: <strong>{data.survey.participantsNum}</strong>
          </div>

          <div className={styles.grid}>
            {data.survey.questions.map((q) => (
              <article key={q.id} className={styles.card}>
                <div className={styles.cardHeader}>
                  <div>
                    <div className={styles.questionText}>{q.text}</div>
                    <div className={styles.questionMeta}>
                      {(() => {
                        const typeLabels: Record<string, string> = {
                          ANSWER: "문답형",
                          MULTIPLE: "객관식",
                          CHECKBOX: "체크박스(중복가능)",
                          DROPDOWN: "드롭다운",
                        };
                        return typeLabels[q.type] || q.type;
                      })()}{" "}
                      · {q.required ? "필수" : "선택"}
                    </div>
                  </div>
                </div>

                {q.type === "ANSWER" ? (
                  <div className={styles.answerContainer}>
                    {q.answers && q.answers.length > 0 ? (
                      <ul className={styles.answerList}>
                        {q.answers.map((a, i) => (
                          <li key={i} className={styles.answerItem}>
                            {a}
                          </li>
                        ))}
                      </ul>
                    ) : (
                      <div className={styles.noAnswer}>응답이 없습니다.</div>
                    )}
                  </div>
                ) : (
                  <div className={styles.optionContainer}>
                    {renderOptionBars(q)}
                  </div>
                )}
              </article>
            ))}
          </div>
        </section>
      )}
    </div>
  );
}
