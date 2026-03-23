import axios from "axios";
import { reissueApi } from "./loginApi";

/**
 * AT-메모리 저장
 */
let accessToken = null;
export const setAccessToken = (token) => {
    accessToken = token;
}
export const getAccessToken = () => accessToken;

const api = axios.create({
    baseURL: "http://localhost:8080",
    timeout: 10000,
    headers: {
        'ContentType': 'application/json'
    },
    withCredentials: true,
})

//요청 인터셉터 - AT 자동 첨부
api.interceptors.request.use((config) => {
    const token = getAccessToken();
    if (token) {
        config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
});

/**
 * [WHAT] 응답인터셉터
 * 
 * [WHY] 만료된 AT로 인가가 필요한 요청을 할 때,
 *      AT재발급 요청 후 원래 요청을 재시도하여서 
 *      사용자 모르게 처리하기 위하여
 * 
 * [흐름] 401 에러 응답 > 
 */

api.interceptors.response.use(
    (response) => response,
    async (error) => {
        if (error.response?.status === 401 && !error.config.url.includes('/auth/reissue')) {
            const originalRequest = error.config;
            try {
                console.log("응답인터셉터 401 감지완료")
                await reissueApi();
                return api(error.config);
            } catch (refreshError) {
                window.location.href = '/'; //이건 나중에 토큰재발급 오류 페이지로 이동??
                return Promise.reject(refreshError);
            }
        }
        return Promise.reject(error);
    }
);

export default api;