import styles from './Input.module.css';

export default function Input({ label, type, name, value, id, error, isRequired, placeholder, ...Rest }) {
    return (
        <>
            <div className={styles.inputDiv}>
                <label htmlFor={id}>{label} :</label>
                <input
                    value={value}
                    name={name}
                    type={type}
                    id={id}
                    required={isRequired}
                    placeholder={placeholder}
                    {...Rest}
                />
                <div className={styles.error}>{error ? error : ''}</div>
            </div>
        </>
    );
}
