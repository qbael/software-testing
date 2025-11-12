import axios from "axios";

const axiosClient = axios.create({
    baseURL: "http://localhost:8080/api",
    withCredentials: true // gửi cookie JWT
});

export default axiosClient;