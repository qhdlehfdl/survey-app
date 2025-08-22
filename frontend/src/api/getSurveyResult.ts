import { refreshAccessToken } from "./refreshToken";

type SurveyPayload = any; // keep flexible; component typing lives in the page

export default async function getSurveyResult(surveyId: number): Promise<SurveyPayload> {
  const makeRequest = async (token?: string) => {
    const headers: Record<string, string> = { "Content-Type": "application/json" };
    if (token) headers["Authorization"] = `Bearer ${token}`;

    const res = await fetch(`/api/survey/${surveyId}/result`, {
      method: "GET",
      headers,
      credentials: "include",
    });

    const json = await res.json().catch(() => null);
    return { res, json };
  };

  let token = localStorage.getItem("accessToken") ?? undefined;
  let { res, json } = await makeRequest(token);

  // HTTP 에러 처리
  if (!res.ok) {
    // 401/403 같은 경우 토큰 재발급 시도해볼 수 있음
    if (res.status === 401 || res.status === 403) {
      const newToken = await refreshAccessToken();
      if (newToken) {
        localStorage.setItem("accessToken", newToken);
        ({ res, json } = await makeRequest(newToken));
      } else {
        throw new Error("인증에 실패했습니다. 다시 로그인해 주세요.");
      }
    } else {
      const text = json && json.message ? json.message : await res.text().catch(() => "");
      throw new Error(text || `HTTP ${res.status}`);
    }
  }

  // API 레벨로는 성공이지만 code 필드로 토큰 만료 표시하는 경우 (data.code == "NP")
  if (json && json.code === "NP") {
    const newToken = await refreshAccessToken();
    if (!newToken) throw new Error("토큰 갱신 실패");
    localStorage.setItem("accessToken", newToken);
    ({ res, json } = await makeRequest(newToken));
    if (!res.ok) {
      throw new Error("토큰 갱신 후 요청에 실패했습니다.");
    }
  }

  return json;
}
