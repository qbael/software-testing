export function validateFields(fieldName, value, isRequired) {

    if (isRequired) {
        if (!value || value.trim() === '') {
            return 'This field is required';
        }
    }

    switch (fieldName) {
        case 'name':
            return /^[a-zA-Z0-9._-]{3,50}$/.test(value)
                ? ''
                : 'Name must be 3-50 characters long and can only contain letters, numbers, ".", "-", or "_"';

        case 'email':
            return /^\S+@\S+\.\S+$/.test(value) ? '' : 'Invalid email. Example: MinhAnfe23@gmail.com';

        case 'phoneNumber':
            return /^\d{10,}$/.test(value) ? '' : 'Phone Number must have at least 10 digits';

        case 'password':
            return /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]{6,100}$/.test(value)
                ? ''
                : 'Password must be 6-100 characters long and contain at least one letter and one number';
        case 'price':
            return /^\d+(\.\d+)?$/.test(value) ? '' : 'Price should have digits only';
        default:
            return '';
    }
}
