import LoginPage from './pages/formPages/LoginPage.jsx';
import RegisterPage from './pages/formPages/registerPage.jsx';
import ProductPage from './pages/productPage/product.jsx';
import ProtectedRoute from './pages/protectedPage.jsx';
import HomePage from "./pages/homePage/HomePage.jsx";


const routes = [
    {
        path: '/',
        element: <HomePage />,
    },
    {
        path: '/login',
        element: <LoginPage />,
    },
    {
        path: '/register',
        element: <RegisterPage />,
    },
    {
        path: '/admin',
        element: (
               <ProtectedRoute>
                   <ProductPage />
               </ProtectedRoute>
        ),
    },
];

export default routes;
