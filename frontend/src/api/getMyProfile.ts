// src/api/profile.ts
import axios from "axios";
import { refreshAccessToken } from "./refreshToken";

type ProfileResponse = {
  nickname: string;
  surveys: any[];
  hasNext?: boolean;
  nextCursor?: number | null;
};

export async function getMyProfile(
  accessToken: string | null,
  lastId?: number | null, // page → lastId
  size?: number
): Promise<ProfileResponse> {
  const makeRequest = async (token: string | null) => {
    const config = {
      headers: token
        ? {
            Authorization: `Bearer ${token}`,
            Accept: "application/json",
          }
        : { Accept: "application/json" },
      params: {} as Record<string, any>,
      withCredentials: true as const, // 서버가 쿠키를 기대할 수 있으니 포함
    };

    if (lastId !== undefined && lastId !== null) config.params.lastId = lastId;
    if (size !== undefined && size !== null) config.params.size = size;

    return axios.get("/api/auth/profile", config);
  };

  try {
    const response = await makeRequest(accessToken);
    return response.data as ProfileResponse;
  } catch (err: any) {
    const status = err?.response?.status;
    const respData = err?.response?.data;

    const needRefresh =
      respData?.code === "NP" || status === 401 || status === 403;

    if (!needRefresh) {
      throw err;
    }

    let newToken: string | null = null;
    try {
      newToken = await refreshAccessToken();
    
    } catch (refreshErr) {
      console.error("refreshAccessToken threw:", refreshErr);
      throw new Error("토큰 갱신 중 오류가 발생했습니다.");
    }

    if (!newToken) {
      return Promise.reject(null);
    }

    try {
      localStorage.setItem("accessToken", newToken);
    } catch (e) {
      console.warn("localStorage set failed:", e);
    }

    try {
      const retryResponse = await makeRequest(newToken);
      return retryResponse.data as ProfileResponse;
    } catch (retryErr: any) {
      console.error("Retry after refresh failed:", retryErr);
      throw retryErr;
    }
  }
}
