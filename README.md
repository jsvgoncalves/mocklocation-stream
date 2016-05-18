MockLocation Stream
==================

The app receives a JSON message via TCP socket and updates the device location using MockLocation API.


**Usage:**

After opening the app press the start button to start the socket. Then just send JSON formatted strings according to the format:

```json
 {
 	"latitude": 41.17794,
 	"longitude": -8.595117,
 	"..." : "..."
 }
```

Default port: 5173


TODO
------------------
- [x] Basic usage
- [ ] Complete speed limit example
- [ ] Provide app just as a service
