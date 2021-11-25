# http_client_t 服務實作說明

- SPO Client 需透過 https 與 SPO Server 或區塊鏈進行溝通，藉此取得上鏈存證與驗證資料，此範例以 curl 來實作
- `http_client_t`檔案 : 
  - [ssl_get.h](../example/spo-client-example/ssl_get.h)
  - [ssl_get.c](../example/spo-client-example/ssl_get.c)

#### `http_client_t` 方法功能說明 :

- `char *spo_get()` : 向 SPO Server get 資訊
- `char *spo_post()` : 向 SPO Server post 資訊
- `char *eth_post()` : 向區塊鏈 post 資訊

#### `http_client_t` 程式如下 :
```C
// 向 SPO Server get 資訊
char *spo_get(const char *url)

// 向 SPO Server post 資訊
char *spo_post(const char *url, const char *postData)

// 向區塊鏈 post 資訊
char *eth_post(const char *url, const char *postData)
```

