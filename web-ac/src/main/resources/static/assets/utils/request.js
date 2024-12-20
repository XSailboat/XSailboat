window.request = axios.create()
request.interceptors.request.use(config => {
  // config.withCredentials = true;
  return config
})

request.interceptors.response.use(
  res => {
    // ...
    return Promise.resolve(res)
  },
  err => {
    // ...
    console.log(err)
    return Promise.reject(err)
  }
)
