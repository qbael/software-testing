import {useEffect,useState} from "react";
import { Navigate } from 'react-router-dom';
import {getCurrentUser} from "../api/authApi.js";

export default function ProtectedRoute({ children }) {
    const [isLoading, setIsLoading] = useState(true);
    const [isLoggedIn, setIsLoggedIn] = useState(false);

    useEffect(() => {
        const checkAuth = async () => {
            try {
                const user = await getCurrentUser();
                if (user) setIsLoggedIn(true);
            } catch {
                setIsLoggedIn(false);
            } finally {
                setIsLoading(false);
            }
        };
        checkAuth();
    }, []);

    if (isLoading) return <div>Loading...</div>;
    if (!isLoggedIn) return <Navigate to="/login" replace />;
    return children;
}
