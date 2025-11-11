export const addProductModel = {
    formName: 'Thêm sản phẩm',
    model: {
        productName: {
            idAttr: `addProductName`,
            label: 'Product Name',
            type: `text`,
            nameAttr: `productName`,
            required: true,
            placeholder: 'Enter product name',
        },
        price: {
            idAttr: `addPrice`,
            label: 'Price',
            type: `number`,
            nameAttr: `price`,
            required: true,
            placeholder: 'Enter price',
        },
        quantity: {
            idAttr: `addQuantity`,
            label: 'Quantity',
            type: `number`,
            nameAttr: `quantity`,
            required: true,
            placeholder: 'Enter quantity',
        },
        description: {
            idAttr: `addDescription`,
            label: 'Description',
            type: `text`,
            nameAttr: `description`,
            required: false,
            placeholder: 'Enter description...',
        },
        category: {
            idAttr: `addCategory`,
            label: 'Category',
            type: `select`,   // đổi từ text sang select
            nameAttr: `category`,
            required: true,
            options: [        // giá trị hợp lệ từ enum backend
                'SMARTPHONE',
                'LAPTOPS',
                'HEADPHONES',
                'CAMERAS'
            ],
        },
    },
};

