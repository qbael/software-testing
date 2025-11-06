export function validateFields(fieldName, value, isRequired) {
    if (isRequired) {
        if (!value || value.trim() === '') {
            return 'This field is required';
        }
    }

    switch (fieldName) {
        case 'name':
            return /^[a-zA-Z\s]{4,}$/.test(value)
                ? ''
                : 'Name should have at least 4 characters and only contain letters and spaces';

        case 'email':
            return /^\S+@\S+\.\S+$/.test(value) ? '' : 'Invalid email. Example: MinhAnfe23@gmail.com';

        case 'phoneNumber':
            return /^\d{10,}$/.test(value) ? '' : 'Phone Number must have at least 10 digits';

        case 'password':
            return /^(?=.*[A-Z])(?=.*[!@#$%^&*])(?=.*\d)[A-Za-z\d!@#$%^&*]{7,}$/.test(value)
                ? ''
                : 'Password must have at least 7 characters, 1 special character, 1 uppercase letter, and a number';
        case 'price':
            return /^\d+(\.\d+)?$/.test(value) ? '' : 'Price should have digits only';
        default:
            return '';
    }
}
