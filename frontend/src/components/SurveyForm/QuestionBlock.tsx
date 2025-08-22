// src/components/SurveyForm/QuestionBlock.tsx
import React, { useRef } from "react";
import { Question, Option, QuestionType } from "./types";
import styles from "./SurveyForm.module.css";

type Props = {
  q: Question;
  onChange: (id: string, patch: Partial<Question>) => void;
  onDelete: (id: string) => void;
};

const TYPE_LABELS: Record<QuestionType, string> = {
  answer: "문답형",
  multiple: "객관식",
  checkbox: "체크박스(중복가능)",
  dropdown: "드롭다운(중복불가)",
};

export default function QuestionBlock({ q, onChange, onDelete }: Props) {
  const textRef = useRef<HTMLDivElement | null>(null);

  // 기존 질문이고 참여자가 1명 이상이면 locked
  const lockedBecauseHasAnswers = !!(
    q.isExisting && (q.participantsNum ?? 0) > 0
  );

  function commitText() {
    // 질문 텍스트는 기존 질문에 응답이 있으면 수정 불가
    if (lockedBecauseHasAnswers) return;
    const text = textRef.current?.innerText ?? "";
    onChange(q.id, { text });
  }

  function setType(t: QuestionType) {
    // 타입 변경 불가(응답이 있는 기존 질문은 변경 금지)
    if (lockedBecauseHasAnswers) return;
    const patch: Partial<Question> = { type: t };
    if (t !== "multiple" && t !== "checkbox" && t !== "dropdown") {
      patch.options = [];
    } else {
      patch.options =
        q.options && q.options.length
          ? q.options
          : [{ text: "옵션 1", isNew: true }];
    }
    onChange(q.id, patch);
  }

  function changeOption(idx: number, val: string) {
    const opts = [...(q.options ?? [])];
    if (!opts[idx]) return;

    // 기존 옵션(isNew === false)은 "lockedBecauseHasAnswers" 일 때 편집 불가
    if (!opts[idx]?.isNew && lockedBecauseHasAnswers) return;

    opts[idx] = { ...opts[idx], text: val };
    onChange(q.id, { options: opts });
  }

  function addOption() {
    // Option 추가는 항상 허용(요구사항)
    const opts = [...(q.options ?? [])];
    opts.push({ text: `옵션 ${(opts.length ?? 0) + 1}`, isNew: true });
    onChange(q.id, { options: opts });
  }

  function removeOption(idx: number) {
    const opts = [...(q.options ?? [])];
    if (!opts[idx]) return;

    // 기존 옵션(isNew === false)은 질문에 응답자가 있으면 삭제 불가,
    // 응답자가 없으면 삭제 허용
    if (!opts[idx]?.isNew && lockedBecauseHasAnswers) return;

    opts.splice(idx, 1);
    onChange(q.id, { options: opts });
  }

  return (
    <div className={styles.qCard}>
      <div className={styles.qLeft}>
        <div className={styles.qDot} />
      </div>

      <div className={styles.qBody}>
        <div
          className={styles.qTitle}
          contentEditable={!lockedBecauseHasAnswers}
          suppressContentEditableWarning
          ref={textRef}
          onBlur={commitText}
          dangerouslySetInnerHTML={{ __html: q.text || "질문을 입력하세요" }}
        />

        <div className={styles.qControlRow}>
          <div className={styles.qType}>
            <label>질문 유형</label>
            <select
              value={q.type}
              onChange={(e) => setType(e.target.value as QuestionType)}
              disabled={lockedBecauseHasAnswers}
            >
              {Object.entries(TYPE_LABELS).map(([val, label]) => (
                <option key={val} value={val}>
                  {label}
                </option>
              ))}
            </select>
          </div>

          <div className={styles.qActions}>
            {/* 삭제 버튼: 기존 질문이고 응답자가 있으면 삭제 불가 */}
            <button
              className={`${styles.small} ${styles.smallDanger}`}
              onClick={() => onDelete(q.id)}
              disabled={lockedBecauseHasAnswers} // 질문 자체 삭제는 금지
            >
              삭제
            </button>

            {/* required 토글: 요구사항에 따라 응답 있어도 변경 허용 */}
            <label className={styles.required}>
              <input
                type="checkbox"
                checked={!!q.required}
                onChange={(e) => {
                  onChange(q.id, { required: e.target.checked });
                }}
              />{" "}
              필수
            </label>
          </div>
        </div>

        {/* 옵션 영역 (객관식/체크박스/드롭다운) */}
        {(q.type === "multiple" ||
          q.type === "checkbox" ||
          q.type === "dropdown") && (
          <div className={styles.options}>
            {(q.options ?? []).map((opt, i) => {
              const isServerOpt = !opt.isNew;
              const optLocked = isServerOpt && lockedBecauseHasAnswers; // 기존 opt + 응답있음 -> locked

              return (
                <div key={i} className={styles.optionRow}>
                  <span className={styles.optionIcon}>
                    {q.type === "multiple"
                      ? "○"
                      : q.type === "checkbox"
                      ? "☐"
                      : "▾"}
                  </span>

                  {/* 기존 옵션은 participation이 있으면 읽기전용, 없으면 편집 가능 */}
                  <input
                    value={opt.text}
                    readOnly={optLocked}
                    onChange={(e) => changeOption(i, e.target.value)}
                    className={styles.optionInput}
                  />

                  {/* 삭제 버튼: 기존 옵션은 participation이 있으면 비활성, 없으면 삭제 가능 */}
                  <button
                    className={styles.small}
                    onClick={() => removeOption(i)}
                    disabled={optLocked}
                  >
                    삭제
                  </button>
                </div>
              );
            })}

            {/* 옵션 추가: 요구사항상 기존 질문에 응답 있어도 옵션 추가는 허용 */}
            <div className={styles.optionAddRow}>
              <button className={styles.small} onClick={addOption}>
                옵션 추가
              </button>
            </div>
          </div>
        )}
      </div>
    </div>
  );
}
