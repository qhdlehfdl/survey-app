// src/api/getSurveyDetail.ts
import { refreshAccessToken } from "./refreshToken";

type FetchResult = {
  res: Response;
  json: any | null;
};

/**
 * 설문 상세를 가져옵니다.
 * - 인자로 optional AbortSignal을 받아 취소를 지원합니다.
 * - 서버가 { code: "NP" } 를 반환하면 refreshAccessToken()을 호출해 토큰을 갱신하고 재시도합니다 (한 번만).
 *
 * @param surveyId 설문 ID
 * @param signal optional AbortSignal (fetch 취소용)
 * @returns API에서 반환한 JSON (성공 시)
 * @throws Error (네트워크/HTTP/토큰갱신 실패 등)
 */
export default async function getSurveyDetail(
  surveyId: number,
  signal?: AbortSignal
): Promise<any> {
  const doFetch = async (token: string | null): Promise<FetchResult> => {
    const headers: Record<string, string> = { Accept: "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const res = await fetch(`/api/survey/${surveyId}`, {
      method: "GET",
      headers,
      signal,
    });

    let json: any = null;
    try {
      // body가 없거나 JSON이 아닐 수 있으니 안전하게 파싱
      json = await res.json();
    } catch (e) {
      json = null;
    }
    return { res, json };
  };

  // 1st attempt with current token
  let accessToken = localStorage.getItem("accessToken");
  let { res, json } = await doFetch(accessToken);
  let didRefresh = false;

  // If API indicates token expired via custom code, try refresh once
  if (json?.code === "NP" && !didRefresh) {
    const newToken = await refreshAccessToken();
    if (!newToken) {
      // refresh 실패: 인증 필요
      throw new Error("토큰 갱신 실패. 다시 로그인해 주세요.");
    }
    localStorage.setItem("accessToken", newToken);
    didRefresh = true;
    accessToken = newToken;
    ({ res, json } = await doFetch(accessToken));
  }

  // HTTP-level error
  if (!res.ok) {
    // Prefer server JSON message if present, otherwise raw text
    const message =
      (json && json.message) ||
      (await res.text().catch(() => "")) ||
      `HTTP ${res.status}`;
    throw new Error(message);
  }

  // If still API-level NP after retry (unlikely), surface that as an error
  if (json?.code === "NP") {
    throw new Error("세션이 만료되었습니다. 다시 로그인해주세요.");
  }

  return json;
}
