from tornado.wsgi import WSGIContainer
from tornado.httpserver import HTTPServer
from tornado.ioloop import IOLoop

import logging
from os import path, listdir, sep
import re
from threading import Thread

import mimetypes
from flask import Response
from flask import Flask
from flask import request

LOG = logging.getLogger(__name__)

IP = "0.0.0.0"
PORT = 8080
FOLDERS = [
    sep.join(__file__.split(sep)[:-1] + ["Videos", ""]),  # Disk-on-key folder
    "D:\\FilmsAndTelevision Videos\\"  # Videos folder in laptop
]

MB = 1 << 20
BUFF_SIZE = 10 * MB


class Stream:
    Files_dict = {}
    shows_dict = {}
    app = Flask(__name__)

    @staticmethod
    def create():
        """
        Initiates and starts the web streaming
        :returns: The thread of the web streaming
        """
        if not Stream.Files_dict:
            Stream.set_files()
        logging.getLogger("tornado.access").disabled = True
        logging.basicConfig(level=logging.INFO)
        http_server = HTTPServer(WSGIContainer(Stream.app))
        http_server.listen(PORT)
        t = Thread(target=IOLoop.instance().start)
        t.start()
        return t

    @staticmethod
    def find_video(folder_path):
        """
        Finds a video and its subtitles languages by its path
        :param folder_path: The path of the video
        :returns: a tuple containing the path of the video,
                  the subtitles languages and the video name
        """
        languages = []
        for subtitles in listdir(folder_path + "Subtitles"):
            languages.append(subtitles.split(".")[0])  # only name, not ending

        # finds video
        video = "Video.mp4"
        for file_ in listdir(folder_path):
            if file_.startswith("Video"):
                video = file_
                break
        return folder_path, languages, video

    @staticmethod
    def find_all_videos(folder):
        """
        Gets all videos (Movies and episodes in shows) from a specific folder
        :param folder: The source folder for the function to search in
        """
        print(folder)
        try:
            sub_folders = listdir(folder)
        except FileNotFoundError:
            print("Videos folder not found")
            return
        for types_path in sub_folders:  # Movies / Shows
            types_path = folder + types_path
            if not path.isdir(types_path):
                continue
            for title_path in listdir(types_path):
                complete_path = types_path + sep + title_path + sep
                if types_path.endswith("Movies"):
                    found = Stream.find_video(complete_path)
                    Stream.Files_dict[title_path] = found
                else:  # Shows
                    show = {}
                    for season in listdir(complete_path):
                        s = season.split()[-1]
                        key = "S" + s + "E"
                        show[s] = []
                        season = complete_path + season + sep
                        for episode in listdir(season):
                            e = episode.split()[-1]
                            key2 = key + e
                            show[s].append(e)
                            video_path = season + episode + sep
                            found = Stream.find_video(video_path)
                            Stream.Files_dict[title_path + key2] = found

                        show[s] = list(map(str, sorted(map(int, show[s]))))
                    Stream.shows_dict[title_path] = show
        print("Done: ", folder)

    @staticmethod
    def set_files():
        """
        Finds all videos in all of the source folders
        If can't find any video, doesn't work
        """
        for folder in FOLDERS:
            Stream.find_all_videos(folder)
        if not Stream.Files_dict:
            print("No videos folder is available, streaming is disabled")

    @staticmethod
    def partial_response(video_path, start, end=None):
        """
        Returns a chunk of a video in bytes
        :param video_path: Path to the video
        :param start: Start time of the chunk
        :param end: End time of the chunk
        :returns: A chunk of a video
        """
        LOG.info('Requested: %s, %s', start, end)
        file_size = path.getsize(video_path)

        # Determine (end, length)
        if end is None:
            end = start + BUFF_SIZE - 1
        end = min(end, file_size - 1)
        end = min(end, start + BUFF_SIZE - 1)
        length = end - start + 1
        print(length)
        print(video_path)
        print("Size:", file_size)
        print("Start: {}, end: {}".format(start, end))

        # Read file
        with open(video_path, 'rb') as fd:
            fd.seek(start)
            bytes_ = fd.read(length)
        assert len(bytes_) == length

        response = Response(
            bytes_,
            206,
            mimetype=mimetypes.guess_type(video_path)[0],
            direct_passthrough=True
        )
        response.headers.add(
            'Content-Range', 'bytes {0}-{1}/{2}'.format(
                start, end, file_size,
            ),
        )
        response.headers.add(
            'Accept-Ranges', 'bytes'
        )
        # LOG.info('Response: %s', response)  # Spams the console
        LOG.info('Response: %s', response.headers)
        return response

    @staticmethod
    def get_range(req):
        """
        Returns a start-end range of a request
        :param req: The request of the client
        :returns: Start-end from the request
        """
        print("Headers:", req.headers)
        range_ = req.headers.get('Range')
        LOG.info('Requested: %s', range_)
        if not range_:
            return 0, None
        m = re.match('bytes=(?P<start>\d+)-(?P<end>\d+)?', range_)
        if m:
            start = m.group('start')
            end = m.group('end')
            start = int(start)
            if end is not None:
                end = int(end)
            print(start, end)
            return start, end
        return 0, None

    @staticmethod
    @app.route("/Watch/<title>")
    def video(title):
        """
        Gets an id of a video and returns a chunk of it
        :param title: The ID of the video
        :returns: A chunk of the video
                  The start and end of the chunk are being decided
                  from the request of the client
        """
        video = Stream.Files_dict.get(title, None)
        assert video is not None

        video = video[0] + video[2]  # video file
        print(video)
        start, end = Stream.get_range(request)
        return Stream.partial_response(video, start, end)

    @staticmethod
    def close():
        """
        Closes the IOLoop
        """
        IOLoop.instance().stop()
