import LoginPage from './Pages/formPages/LoginPage.jsx';
import RegisterPage from './Pages/formPages/RegisterPage.jsx';
import ProductPage from './Pages/productPage/Product.jsx';
import ProtectedRoute from './Pages/ProtectedPage.jsx';
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
