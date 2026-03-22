import { useEffect, useState } from "react"
import { logoutApi, userInfoApi } from "../api/loginApi";
import { getAccessToken, setAccessToken } from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";

export const Home = () => {
    const [user, setUser] = useState("");
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const fetchUser = async () => {
        setUser("");
        try {
            const { data } = await userInfoApi();
            setUser(data.name);
        } catch (err) {
            setError(err.response?.data?.message);
        }
    }
    useEffect(() => {

        fetchUser();
    }, []);

    const handleLogout = async () => {
        //로그아웃api RT삭제
        try {
            await logoutApi();
            setAccessToken(null);
        } catch (err) {
            setError(err.response?.data?.message);
        }
        //로그인페이지 이동
        navigate('/')
    }

    const handleTest = async () => {
        const { data } = await userInfoApi();
        console.log(data);
    }

    return (
        <div>
            <h1>{user}님 안녕하세요</h1>
            <p>AT:{getAccessToken() ?? "없음"}</p>
            <button onClick={handleLogout}>로그아웃</button>
            <button onClick={handleTest}>테스트</button>
        </div>
    )
}