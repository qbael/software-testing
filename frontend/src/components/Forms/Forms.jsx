import { useEffect, useState } from 'react';
import Input from '../Inputs/Input';
import styles from './Forms.module.css';
import { validateFields } from '../../utils/validations/validate';
import closeIcon from '../../assets/closeIcon.svg';

export default function Form({ formModel, onSubmit, closeIconDisplay = false, toCloseForm, object = null }) {
    const initialDataErrorObj = Object.fromEntries(
        Object.entries(formModel.model).map(([key]) => [key, { value: '', error: '' }])
    );

    const [DataErrorObj, setDataErrorObj] = useState(initialDataErrorObj);

    useEffect(() => {
        if (object) {
            const validateData = Object.fromEntries(
                Object.entries(formModel.model).map(([key, model]) => [
                    key,
                    { value: object[key] ?? object[model.matchField] ?? '', error: '' },
                ])
            );
            setDataErrorObj(validateData);
        }
    }, [object]);

    const validateSingleField = (key, value) => validateFields(key, value, formModel.model[key].required);

    const validateMatchingField = (key, value) => {
        const matchField = formModel.model[key].matchField;
        if (matchField && value !== DataErrorObj[matchField].value) {
            return formModel.model[key].errorMsg || 'the field not match';
        }
        return '';
    };

    const handleInputBlur = (e) => {
        const { name, value } = e.target;
        const error = validateSingleField(name, value) || validateMatchingField(name, value);
        setDataErrorObj((prev) => ({
            ...prev,
            [name]: { ...prev[name], error },
        }));
    };

    const handleInputChange = (e) => {
        const { name, value } = e.target;
        setDataErrorObj((prev) => ({
            ...prev,
            [name]: { ...prev[name], value },
        }));
    };

    const checkValidity = () => {
        let formValidity = true;
        const cloneObj = { ...DataErrorObj };
        Object.entries(cloneObj).forEach(([key, model]) => {
            model.error = validateSingleField(key, String(model.value)) || validateMatchingField(key, String(model.value));
            if (model.error) formValidity = false;
        });
        setDataErrorObj(cloneObj);
        return formValidity;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (checkValidity()) {
            // Chuyển sang object chỉ chứa value
            const payload = Object.fromEntries(
                Object.entries(DataErrorObj).map(([key, val]) => [key, val.value])
            );
            onSubmit(payload);
        }
    };

    return (
        <form onSubmit={handleSubmit} noValidate>
            <img
                onClick={toCloseForm}
                className={closeIconDisplay ? styles.closeIconDisplay : styles.hide}
                src={closeIcon}
                alt="close icon"
            />
            <h1>{formModel.formName}</h1>
            <div className={styles.fieldsWrapper}>
                {object?.id && <div className={styles.idField}>ID: {object.id}</div>}
                {Object.entries(formModel.model).map(([key, model]) => (
                    <div key={key}>
                        <Input
                            onBlur={handleInputBlur}
                            onChange={handleInputChange}
                            type={model.type}
                            name={model.nameAttr}
                            value={DataErrorObj[key].value}
                            id={model.idAttr}
                            label={model.label}
                            error={DataErrorObj[key].error}
                            isRequired={model.required}
                            placeholder={model.placeholder}
                            options={model.options} // thêm cho select
                        />
                    </div>
                ))}
            </div>
            <button type="submit">{formModel.formName}</button>
        </form>
    );
}
