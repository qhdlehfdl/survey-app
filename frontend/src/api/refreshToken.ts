// src/api/auth.ts
import axios from "axios";

/**
 * refreshAccessToken:
 *  - 성공: 서버가 code === "SU" 와 token을 보내면 token 반환
 *  - refresh invalid (code === "IRT"): 전역 이벤트 'auth:refreshInvalid' 발생 후 null 반환
 *  - 기타 실패: null 반환
 */
export async function refreshAccessToken(): Promise<string | null> {
  try {
    const response = await axios.post(
      "/api/auth/refresh",
      {},
      { withCredentials: true, headers: { Accept: "application/json" } }
    );

    const data = response.data ?? {};
    const code: string | undefined = data.code;

    if (code === "SU") {
      const newToken = data.newToken ?? data.token ?? null;
      return typeof newToken === "string" && newToken.length > 0
        ? newToken
        : null;
    }

    else {
      // refresh token invalid -> 전역 이벤트 발생
      if (typeof window !== "undefined" && "dispatchEvent" in window) {
        try {
          // CustomEvent로 상세 정보 보낼 수 있음 (선택)
          const evt = new CustomEvent("auth:refreshInvalid", {
            detail: { reason: "IRT" },
          });
          window.dispatchEvent(evt);
        } catch (e) {
          // fallback: 일반 Event
          window.dispatchEvent(new Event("auth:refreshInvalid"));
        }
      }
      return null;
    }

    // 그 외
    console.warn("Unexpected refresh response:", data);
    return null;
  } catch (error) {
    console.error("토큰 갱신 실패:", error);
    return null;
  }
}
