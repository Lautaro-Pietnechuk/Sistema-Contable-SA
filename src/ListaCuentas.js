// src/components/listaCuentas.js

import React from 'react';
import axios from 'axios';

const AccountList = ({ accounts, onAccountDeleted }) => {
    const handleDelete = async (accountId) => {
        try {
            await axios.delete(`http://localhost:8080/api/accounts/${accountId}`);
            onAccountDeleted(accountId); // Notify parent component about the deleted account
        } catch (error) {
            console.error('Error deleting account:', error);
        }
    };

    return (
        <ul>
            {accounts.map(account => (
                <li key={account.id}>
                    {account.name}
                    <button onClick={() => handleDelete(account.id)}>Eliminar</button>
                </li>
            ))}
        </ul>
    );
};

export default AccountList;
