// src/components/SurveyForm/types.ts
export type QuestionType = "answer" | "multiple" | "checkbox" | "dropdown";

export type Option = {
  // 기존 서버 옵션은 isNew: false, 클라이언트에서 추가한 옵션은 isNew: true
  text: string;
  isNew?: boolean;
};

export type Question = {
  // id는 서버의 questionId(UUID) 또는 클라이언트 생성 UUID
  id: string;
  text: string;
  type: QuestionType;
  options: Option[]; // 옵션 목록 (서버 옵션은 isNew=false)
  required: boolean;

  // 메타 (서버에서 올 때 세팅)
  isExisting?: boolean; // 서버에서 온 기존 질문이면 true
  participantsNum?: number; // 서버가 준 질문별 참여자 수

  order?: number;
};
