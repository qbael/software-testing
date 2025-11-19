// src/api/authApi.js
import axiosClient from "./axiosClient";

export async function login(username, password) {
    const res = await axiosClient.post("/auth/login", {
        username,
        password,
    });
    return res.data;
}

export async function getCurrentUser() {
    const res = await axiosClient.get("/auth/current");
    return res.data;
}

export async function register(username, password, verifyPassword)
{
    const res = await axiosClient.post("/auth/register", {
        username,
        password,
        verifyPassword,
    });
    return res.data;
}
export async function logout(){
    const res = await axiosClient.post("/auth/logout");
    return res.data
}



