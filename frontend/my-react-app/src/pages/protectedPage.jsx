import {useEffect,useState} from "react";
import { Navigate } from 'react-router-dom';

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
    return isLoggedIn ? children : <Navigate to="/login" />;
}
