// src/api/auth.ts
import axios from "axios";

export async function refreshAccessToken(): Promise<string | null> {
  try {
    const response = await axios.post(
      "/api/auth/refresh",
      {},
      {
        withCredentials: true, // refresh token 쿠키 포함
      }
    );

    // 서버에서 새 access token을 반환한다고 가정
    const { newToken } = response.data;
    return newToken;
  } catch (error) {
    console.error("토큰 갱신 실패:", error);
    return null;
  }
}
