# ---- Build Stage ----
ARG GO_VERSION=${goVersion!"1.21"}
FROM golang:${r"${GO_VERSION}"}-alpine AS builder

# Set the Current Working Directory inside the container
WORKDIR /app

# Copy go mod and sum files
COPY go.mod go.sum ./

# Download all dependencies. Dependencies will be cached if the go.mod and go.sum files are not changed
RUN go mod download
RUN go mod verify

# Copy the source code into the container
COPY . .

# Build the Go app
# CGO_ENABLED=0 produces a statically linked binary (no external C libraries needed)
# -o specifies the output file name
# -ldflags="-w -s" strips debug information and symbols to reduce binary size (optional)
# ./ refers to building the package in the current directory (where main.go is)
RUN CGO_ENABLED=0 GOOS=linux go build -a -installsuffix cgo -ldflags="-w -s" -o /app/${appBinaryName} .

# ---- Runtime Stage ----
ARG ALPINE_VERSION=${alpineVersion!"3.19"}
FROM alpine:${r"${ALPINE_VERSION}"}

# Set the Current Working Directory inside the container
WORKDIR /app

# Copy the Pre-built binary file from the previous stage
COPY --from=builder /app/${appBinaryName} .

# Add any necessary CA certificates (if your app makes HTTPS calls to external services)
# RUN apk --no-cache add ca-certificates

# Expose port ${exposedPort} to the outside world
EXPOSE ${exposedPort}

# Command to run the executable
CMD ["./${appBinaryName}"]