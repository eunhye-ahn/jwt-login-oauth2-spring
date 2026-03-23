import { useEffect } from "react"
import { setAccessToken } from "../api/axiosInstance";
import { useNavigate } from "react-router-dom";

export const Callback = () => {
    const navigate = useNavigate();

    useEffect(() => {
        const params = new URLSearchParams(window.location.search);
        const accessToken = params.get("accessToken");

        if (accessToken) {
            setAccessToken(accessToken);
            navigate("/home");
        }
    }, []);

    return (
        <div>
            로그인중..
        </div>
    )
}