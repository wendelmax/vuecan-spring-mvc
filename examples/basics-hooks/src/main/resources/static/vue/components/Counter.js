const { useState, useEffect, createElement } = Vue;

export function Counter({ initialValue = 0 }) {
    const [count, setCount] = useState(initialValue);
    const [lastAction, setLastAction] = useState('None');

    // Using the global Vuecan Context
    const vuecanContext = window.__VUECAN_CONTEXT__ || {};

    useEffect(() => {
        console.log(`Count changed to: ${count}`);
    }, [count]);

    return createElement('div', {
        className: 'counter-container',
        style: { border: vuecanContext.siteTheme === 'dark' ? '2px solid #555' : '1px solid #ccc', padding: '20px', borderRadius: '8px' }
    },
        createElement('h2', null, `Count: ${count}`),
        createElement('p', null, `Last Action: ${lastAction}`),
        vuecanContext.flashMessage && createElement('p', {
            style: { padding: '10px', background: '#d4edda', color: '#155724', borderRadius: '4px', margin: '10px 0' }
        }, vuecanContext.flashMessage),
        createElement('p', { style: { fontSize: '0.8em', color: '#666' } }, `Theme: ${vuecanContext.siteTheme}, User: ${vuecanContext.userRole}`),
        createElement('div', { style: { display: 'flex', gap: '10px', justifyContent: 'center', marginTop: '10px' } },
            createElement('button', {
                onClick: () => {
                    setCount(c => c + 1);
                    setLastAction('Increment');
                },
                style: { padding: '10px 20px', cursor: 'pointer', background: '#4CAF50', color: 'white', border: 'none', borderRadius: '4px' }
            }, '+'),
            createElement('button', {
                onClick: () => {
                    setCount(c => c - 1);
                    setLastAction('Decrement');
                },
                style: { padding: '10px 20px', cursor: 'pointer', background: '#f44336', color: 'white', border: 'none', borderRadius: '4px' }
            }, '-')
        )
    );
}
