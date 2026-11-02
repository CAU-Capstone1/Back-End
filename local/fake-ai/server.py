"""
로컬 개발용 가짜 AI 서버.
실제 MusicGen 서버와 같은 POST /generate (multipart: melody_file, description, duration) 를 받아
duration 길이의 사인파 WAV를 반환한다. 표준 라이브러리만 사용한다.
"""
import io
import math
import os
import re
import struct
import time
import wave
from http.server import BaseHTTPRequestHandler, ThreadingHTTPServer

SAMPLE_RATE = 32000                                  # MusicGen 출력과 같은 32kHz
DELAY_SEC = float(os.environ.get("FAKE_AI_DELAY_SEC", "3"))  # 생성 시간 흉내
MAX_DURATION = 30


def make_wav(seconds: int) -> bytes:
    buf = io.BytesIO()
    with wave.open(buf, "wb") as w:
        w.setnchannels(1)
        w.setsampwidth(2)
        w.setframerate(SAMPLE_RATE)
        frames = bytearray()
        for i in range(SAMPLE_RATE * seconds):
            sample = int(8000 * math.sin(2 * math.pi * 440 * i / SAMPLE_RATE))
            frames += struct.pack("<h", sample)
        w.writeframes(bytes(frames))
    return buf.getvalue()


class Handler(BaseHTTPRequestHandler):
    def read_body(self) -> bytes:
        # WebClient는 multipart 요청을 chunked 전송으로 보내므로 Content-Length가 없을 수 있다
        if "chunked" in self.headers.get("Transfer-Encoding", "").lower():
            body = bytearray()
            while True:
                size = int(self.rfile.readline().split(b";")[0].strip(), 16)
                if size == 0:
                    self.rfile.readline()
                    return bytes(body)
                body += self.rfile.read(size)
                self.rfile.readline()
        return self.rfile.read(int(self.headers.get("Content-Length", 0)))

    def do_POST(self):
        if self.path != "/generate":
            self.send_error(404)
            return

        body = self.read_body()
        if b'name="melody_file"' not in body:
            self.send_error(422, "melody_file is required")
            return

        match = re.search(rb'name="duration"\r\n(?:[^\r\n]*\r\n)*\r\n(\d+)', body)
        duration = min(int(match.group(1)) if match else 10, MAX_DURATION)

        time.sleep(DELAY_SEC)
        wav = make_wav(duration)

        self.send_response(200)
        self.send_header("Content-Type", "audio/wav")
        self.send_header("Content-Length", str(len(wav)))
        self.end_headers()
        self.wfile.write(wav)


if __name__ == "__main__":
    print(f"fake AI server listening on :8000 (delay={DELAY_SEC}s)", flush=True)
    ThreadingHTTPServer(("0.0.0.0", 8000), Handler).serve_forever()
