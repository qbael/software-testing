// babel.config.js
module.exports = {
    presets: [
        ['@babel/preset-env', { targets: { node: 'current' } }], // cho Jest/Node
        ['@babel/preset-react', { runtime: 'automatic' }], // React 17+ JSX transform
    ],
};
