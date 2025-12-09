import {validateFields} from '../../../src/utils/validations/validate.js'

test.each([
    ['', 'empty string'],
    [' ', 'only whitespace']
])('should return when username is blank', (username) => {
    const result = validateFields('name', username, true)
    expect(result).toBe('This field is required')
})