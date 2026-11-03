# HomeServo

A responsive home services booking website built with Java 17 and the JDK's built-in HTTP server. It includes an in-memory booking API and a mobile-friendly booking experience.

## Run

```sh
mvn package
java -cp target/classes com.homeservo.HomeServoApplication
```

Then open [http://localhost:8080](http://localhost:8080). Set `PORT` to use a different port.

## Publish a demo

This project includes a Dockerfile for deployment to a Docker-capable web host. Create a separate Git repository containing the contents of this `homeservo` folder, push it to GitHub, then create a **Web Service** on Render and connect that repository. Select **Docker** as the runtime. Render will build from the Dockerfile and provide a public `onrender.com` URL. You can connect a custom domain in the Render service settings.

This is a prototype: booking requests are stored in memory and are cleared when the server restarts. Connect persistent storage and a real booking/worker workflow before using it for customer bookings.

## API

- `GET /api/services` returns the available services and starting prices.
- `POST /api/bookings` accepts booking details as JSON and returns a booking ID. Bookings are held in memory and reset when the app stops.
