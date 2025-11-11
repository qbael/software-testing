import styles from './Input.module.css';

export default function Input({ label, type, name, value, id, error, isRequired, placeholder, options = [], ...Rest }) {
    return (
        <div className={styles.inputDiv}>
            <label htmlFor={id}>{label} :</label>

            {type === 'select' ? (
                <select
                    name={name}
                    id={id}
                    value={value}
                    required={isRequired}
                    {...Rest}
                >
                    <option value="">--Chọn--</option>
                    {options.map((opt) => (
                        <option key={opt} value={opt}>
                            {opt}
                        </option>
                    ))}
                </select>
            ) : (
                <input
                    value={value}
                    name={name}
                    type={type}
                    id={id}
                    required={isRequired}
                    placeholder={placeholder}
                    {...Rest}
                />
            )}

            <div className={styles.error}>{error ? error : ''}</div>
        </div>
    );
}
