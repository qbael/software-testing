import * as authApi from "../../../src/api/authApi.js";
import userEvent from "@testing-library/user-event";
import LoginPage from "../../../src/pages/formPages/LoginPage.jsx";
import {render, screen, waitFor} from "@testing-library/react";
import {BrowserRouter} from "react-router-dom";

const mockNavigate = jest.fn()
jest.mock('react-router-dom', () => ({
    ...jest.requireActual('react-router-dom'),
    useNavigate: () => mockNavigate
}))

jest.mock('../../../src/api/authApi.js')

global.alert = jest.fn()

const renderWithRouter = (component) => {
    return render(<BrowserRouter>{component}</BrowserRouter>)
}

beforeEach(() => {
    jest.clearAllMocks()
})

test('login and getCurrent success', () => {
    const loginResponse = {id: '123', username: 'username'}
    const currentResponse = {id: '123', username: 'username'}

    authApi.login.mockResolvedValue(loginResponse)
    authApi.getCurrentUser.mockResolvedValue(currentResponse)

    expect(authApi.login).toBeDefined()
    expect(authApi.getCurrentUser).toBeDefined()
})

test('login success', async () => {
    authApi.login.mockResolvedValue({id: '123', username: 'username'})
    authApi.getCurrentUser.mockResolvedValue({id: '123', username: 'username'})

    const user = userEvent.setup()
    renderWithRouter(<LoginPage />)

    await user.type(screen.getByLabelText(/^Name/i), 'username')
    await user.type(screen.getByLabelText(/^Password/i), 'Password123')
    await user.click(screen.getByRole('button', {name: /đăng nhập/i}))

    await waitFor(() => {
        expect(authApi.login).toHaveBeenCalledWith('username', 'Password123')
        expect(authApi.getCurrentUser).toHaveBeenCalled()
        expect(global.alert).toHaveBeenCalledWith("Đăng nhập thành công!")
        expect(mockNavigate).toHaveBeenCalledWith("/admin")
    })
})

test('user not found', async () => {
    authApi.login.mockRejectedValue({
        response: {status: 404, data: 'Không tìm thấy người dùng'}
    })

    const user = userEvent.setup()
    renderWithRouter(<LoginPage />)

    await user.type(screen.getByLabelText(/^Name/i), 'notexist')
    await user.type(screen.getByLabelText(/^Password/i), 'Password123')
    await user.click(screen.getByRole('button', {name: /đăng nhập/i}))

    await waitFor(() => {
        expect(authApi.login).toHaveBeenCalled()
        expect(global.alert).toHaveBeenCalledWith('Không tìm thấy người dùng')
        expect(mockNavigate).not.toHaveBeenCalled()

    })
})