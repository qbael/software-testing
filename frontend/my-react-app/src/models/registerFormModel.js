export const registerFormModel = {
    formName: 'Register',
    model: {
        name: {
            idAttr: `name`,
            label: 'Name',
            type: `text`,
            nameAttr: `name`,
            required: true,
            placeholder: 'Enter your Name',
        },
        password: {
            idAttr: `password`,
            label: 'Password',
            type: `password`,
            nameAttr: `password`,
            required: true,
            placeholder: 'Enter Your Password',
        },
        confirmPassword: {
            idAttr: `confirmPassword`,
            label: 'Confirm Password',
            type: `password`,
            nameAttr: `confirmPassword`,
            matchField: 'password',
            errorMsg: 'confirming password do not match',
            required: true,
            placeholder: 'Enter Your Password',
        },
    },
};
