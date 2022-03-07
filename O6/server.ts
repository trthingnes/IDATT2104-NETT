import { Server, IncomingMessage, createServer, STATUS_CODES } from "http"
import crypto from "crypto"
import { Duplex } from "stream"

const host = "localhost"
const port = 8888

let sockets: Array<Duplex> = []
let server: Server = createServer((message) => {
    console.info(`Client connected from ${message.socket.remotePort}.`)
})

server.on("upgrade", wsOnUpgrade)
server.listen(port, host, () => {
    console.info(`Server running on http://${host}:${port}.`)
})

// * Websocket handlers
/**
 * Performs websocket handshake and sets up data handler.
 * @param request Incoming request from {@link Server}.
 * @param socket {@link Duplex} stream for receiving/sending data.
 */
function wsOnUpgrade(request: IncomingMessage, socket: Duplex) {
    if (!isValidWebsocketMessage(request)) {
        socket.end(status(400))
        return
    }

    let accept = generateAcceptHash(request.headers["sec-websocket-key"])
    let headers = [
        status(101),
        "Connection: Upgrade",
        "Upgrade: WebSocket",
        "Sec-WebSocket-Protocol: json",
        `Sec-WebSocket-Accept: ${accept}`,
    ]

    socket.write(headers.join("\r\n").concat("\r\n\r\n"))
    sockets.push(socket)

    socket.on("data", (buffer) => wsOnData(buffer, socket))
}

/**
 * Receives data and sends it to all other sockets.
 * @param buffer {@link Buffer} containing the received data.
 */
function wsOnData(buffer: Buffer, socket: Duplex) {
    try {
        let offset = 0
        let { rsv, opcode } = interpretFirstByte(buffer.readUInt8(offset++))
        let { masked, length } = interpretSecondByte(buffer.readUInt8(offset++))

        if (rsv) {
            throw new Error("RSV flags must not be set.")
        }

        if (opcode != 1) {
            throw new Error("Only text frames are supported.")
        }

        if (!masked) {
            throw new Error("Incoming data must be masked.")
        }

        if (length > 125) {
            if (length === 126) {
                length = buffer.readUInt16BE(offset)
                offset += 2
            } else {
                throw new Error("Large payloads are not supported.")
            }
        }

        let masks = []
        for (let i = 0; i < 4; i++) {
            masks.push(buffer.readUInt8(offset++))
        }

        let data = Buffer.alloc(length)
        for (let i = 0; i < length; ++i) {
            let source = buffer.readUInt8(offset++)
            data.writeUInt8(masks[i % 4] ^ source, i)
        }

        let message = JSON.parse(data.toString())

        if (message) {
            let response = generateResponseMessage({
                message: message.message,
            })
            console.log(response.toString())
            sockets.forEach((s) => {
                s.write(response, () => {
                    console.log("Write success")
                })
            })
        } else {
            sockets = sockets.filter((s) => s !== socket)
            sockets.forEach((s) => {
                s.write(generateResponseMessage({ connected: sockets.length }))
            })
        }
    } catch (e) {
        console.error(e)
    }
}

// * Helper functions
function isValidWebsocketMessage(message: IncomingMessage) {
    return (
        message.headers["connection"] &&
        message.headers["upgrade"] &&
        message.headers["connection"].toLowerCase() === "upgrade" &&
        message.headers["upgrade"].toLowerCase() === "websocket" &&
        message.headers["sec-websocket-key"] &&
        message.headers["sec-websocket-version"] === "13" &&
        message.headers["sec-websocket-protocol"] &&
        message.headers["sec-websocket-protocol"]
            .toLowerCase()
            .indexOf("json") != -1
    )
}

function interpretFirstByte(firstByte: number) {
    let rsv = Boolean(firstByte & 0b01110000)
    let opcode = firstByte & 0b00001111

    return { rsv, opcode }
}

function interpretSecondByte(secondByte: number) {
    let masked = Boolean(secondByte & 0b10000000)
    let length = secondByte & 0b01111111

    return { masked, length }
}

function generateAcceptHash(key: string | undefined) {
    const websocketguid = "258EAFA5-E914-47DA-95CA-C5AB0DC85B11"

    return key
        ? crypto
              .createHash("sha1")
              .update(key.trim().concat(websocketguid), "binary")
              .digest("base64")
        : ""
}

function generateResponseMessage(object: any) {
    let json = JSON.stringify(object)
    let jsonLength = Buffer.byteLength(json)
    let extraLength = jsonLength >= 126
    let payloadLength = jsonLength + (extraLength ? 2 : 0)
    let data = Buffer.alloc(2 + payloadLength)
    let offset = 0

    data.writeUInt8(0b10000001, offset++)
    data.writeUInt8(payloadLength, offset++)

    if (extraLength) {
        data.writeUInt16BE(jsonLength, offset)
        offset += 2
    }

    data.write(json, offset)

    return data
}

function status(code: number) {
    return `HTTP/1.1 ${code} ${STATUS_CODES[code]}`
}
