export const loginFormModel = {
    formName: 'Đăng nhập',
    model: {
        name: {
            idAttr: `name`,
            label: 'Name',
            type: `text`,
            nameAttr: `name`,
            required: true,
            placeholder: 'Enter Your Name',
        },
        password: {
            idAttr: `password`,
            label: 'Password',
            type: `password`,
            nameAttr: `password`,
            required: true,
            placeholder: 'Enter Your Password',
        },
    },
};
