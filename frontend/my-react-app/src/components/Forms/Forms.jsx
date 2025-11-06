import { useEffect, useState } from 'react';
import Input from '../Inputs/Input';
import styles from './Forms.module.css';
import { validateFields } from '../../utils/validations/validate';
import closeIcon from '../../assets/closeIcon.svg';

export default function Form({ formModel, onSubmit, closeIconDisplay = false, toCloseForm, object = null }) {
    const initialDataErrorObj = Object.fromEntries(
        Object.entries(formModel.model).map(([key]) => [key, { value: '', error: '' }]),
    );

    const [DataErrorObj, setDataErrorObj] = useState(initialDataErrorObj);

    useEffect(() => {
        if (object) {
            const validateData = Object.fromEntries(
                Object.entries(formModel.model).map(([key, model]) => [
                    key,
                    { value: `${object ? (object[key] ? object[key] : object[model.matchField]) : ''}`, error: '' },
                ]),
            );
            setDataErrorObj(validateData);
        }
    }, [object]);

    const validateSingleField = (key, value) => {
        let error = validateFields(key, value, formModel.model[key].required);
        return error;
    };
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
            [name]: {
                ...prev[name],
                error: error,
            },
        }));
    };

    const checkValidity = () => {
        let formValidity = true;
        const cloneObj = { ...DataErrorObj };
        Object.entries(cloneObj).forEach(([key, model]) => {
            const value = model.value;
            model.error = validateSingleField(key, value) || validateMatchingField(key, value);
            if (model.error !== '') formValidity = false;
        });
        setDataErrorObj(cloneObj);
        return formValidity;
    };

    const resetInputVaLue = () => {
        const cloneObj = { ...DataErrorObj };
        Object.entries(cloneObj).forEach(([_, model]) => {
            model.value = '';
        });
        setDataErrorObj(cloneObj);
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        const formValidity = checkValidity();
        if (formValidity) {
            onSubmit(DataErrorObj);
            resetInputVaLue();
        }
    };

    const handleInputChange = (event) => {
        const { name, value } = event.target;
        setDataErrorObj((prev) => ({
            ...prev,
            [name]: {
                value,
            },
        }));
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
                            onChange={(e) => handleInputChange(e)}
                            type={model.type}
                            name={model.nameAttr}
                            value={DataErrorObj[key].value}
                            id={model.idAttr}
                            label={model.label}
                            error={DataErrorObj[key].error}
                            isRequired={model.required}
                            placeholder={model.placeholder}
                        />
                    </div>
                ))}
            </div>
            <button type="submit">{formModel.formName}</button>
        </form>
    );
}
