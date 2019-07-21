import socket
from string import whitespace
from random import choice
from types import MethodType, FunctionType
from threading import Thread, Lock

from database_access import Database
from title import Title as TitleClass
from values import ARRAY_SEPARATORS
from values import find_sep, split_string, join_arr, is_english
from web_streaming import Stream

# Emails
import smtplib
# from email.mime.text import MIMEText
# from email.mime.multipart import MIMEMultipart

# Char count to fill the char count of the length of a string
MESSAGE_LENGTH = 8
# IP of the computer
IP = "0.0.0.0"
# Port of the server
SERVER_PORT = 2000
# Function types
FUNCTION = (MethodType, FunctionType)
# Default IMDb link
IMDB_LINK = "https://www.imdb.com/title/"
# Stop condition
stop_condition = False


class Server:
    """
    Class that represents the single server that controls all clients
    """
    actions = {}
    email_body_template = ""

    def __init__(self, ip, port):
        """
        Initiates the servers
        :param ip: IP of the server ("0.0.0.0" - it takes the computer's IP)
        :param port: Port of the server
        """
        self.database = Database()
        try:
            self.socket = socket.socket(socket.AF_INET, socket.SOCK_STREAM)
            self.socket.bind((ip, port))

            self.socket.listen(5)
            self.thread_counter = 0
            print("CONNECTED TO:", self.socket.getsockname())
            # Prevent using multiple devices
            self.lock = Lock()

            # Emails
            self.email_address, password = self.database.app_email_login()
            if self.email_address is None:
                print("No email address was initiated.\n~Shutting down~")
                self.database.shut_down()
                exit(-2)

            self.email_server = smtplib.SMTP("smtp.gmail.com", 587)
            self.email_server.ehlo()
            self.email_server.starttls()
            print(self.email_address, password)
            self.email_server.login(self.email_address, password)
            print("LOGIN")

            with open("mail_format.txt") as f:
                Server.email_body_template = f.read() % self.email_address
        except Exception as msg:
            print("Exception: ", msg)
            exit(-999)
        if not Server.actions:
            Server.bind_commands()

    @staticmethod
    def bind_commands():
        """
        Binds the actions dictionary (Server.actions) to the actions
        of the server which are accessible to the client
        """
        items = dir(Server)
        for item in items:
            a = getattr(Server, item)
            if type(a) in FUNCTION and item.startswith("act_"):
                name = "_".join(item.split("_")[1:]).upper()
                Server.actions[name] = a
        print(list(Server.actions.keys()))

    @staticmethod
    def receive_client_request(client):
        """
        Receives and returns a string from the client
        :param client: The client socket to receive a message from
        :returns: Received message
        """
        data = ""
        data_size = client.recv(MESSAGE_LENGTH).decode()
        if data_size.isdigit():
            data = client.recv(int(data_size)).decode()
        # print("Received: ", repr(data))
        return data

    @staticmethod
    def send_client_response(client, message):
        """
        Gets a string and sends it to the client
        :param client: The client socket to send the message to
        :param message: A message
        """
        if isinstance(message, tuple):
            print("A server-side error has occurred.")
            return
            # print("Sending tuple for some reason... fix please")
            # for msg in message:
            #     Server.send_client_response(client, msg)
            # return
        command_len = str.encode(str(len(message)).zfill(MESSAGE_LENGTH))
        message = str.encode(message)
        if not len(message):
            return
        message1 = command_len + message
        # print("Sending:", message1)
        client.send(message1)

    def send_pin(self, email, pin):
        """
        Sends PIN message to a given email
        :param email: Destination email
        :param pin: PIN code to send to the email
        :returns: Either a success message or an error message
        """
        msg = Server.email_body_template.format(email, pin)
        try:
            print("Sends pin code to: ", email)
            self.email_server.sendmail(self.email_address, email, msg)
            return "Success"
        except (smtplib.SMTPRecipientsRefused, smtplib.SMTPSenderRefused):
            return "Error: Bad email address"

    def act_register(self, data):
        """
        Registers a user to the database
        :param data: User (An array containing the email of the user,
                     the password of the user, the birth date of the user
                     and the name of the user)
        :returns: The database's answer: Success / Failure message
        """
        print("Attempting register with data:", data)
        data = split_string(data)
        answer, pin = self.database.register(data)
        if answer == "SUCCESS":
            pin_answer = self.send_pin(data[1], pin)
            if pin_answer.startswith("Error"):
                answer = pin_answer
                self.database.delete_user(data[0])
        print("Database's answer:", answer)
        return str(answer)

    def act_confirm_register(self, data):
        """
        Confirms the registration of a user
        :param data: An array containing the email address of the user
                     and the confirmation PIN code of the user
        :returns: The database's answer: Success / Failure message
        """
        print("Attempting to confirm register with data: ", data)
        answer = self.database.activate_user(split_string(data))
        print("Database's answer:", answer)
        return answer

    def act_login(self, data):
        """
        :param data: string that contains both email and password
                     that the user tried to login with
        :return: Success case: returns "Success" and username of the user
                 Failure case: reason for failed login
        """
        print("Attempting to login with data:", data)
        answer = self.database.check_user_login(*split_string(data))
        print("Database's answer:", answer)
        if isinstance(answer, tuple):
            answer = list(answer)
            answer[1] = join_arr(*eval(answer[1]))
            answer[2] = eval(answer[2])
            if len(answer[2]):
                answer[2] = join_arr(*answer[2])
            else:
                answer = answer[:2]
            answer = "Success:" + join_arr(*answer)
        return answer

    def act_search(self, data):
        """
        Attempts to search title with given data
        :param data: [Search term]
        :returns: The title it found or an error message if it
                  couldn't find any title
        """
        print("Attempting search with data:", data)
        records = self.database.search_title(split_string(data))
        if records == "Error":
            return "Error"
        # [[Movies...], [Shows...]]
        answer = records
        print("Database's answer:", answer)
        return answer

    def act_request(self, data):
        """
        Requests an attribute of a given user
        :param data: An array containing the request of the client,
                     the email address of the user and other variable
                     (depending of the request of the client)
                     (See get_user_attr in database_access.py)
        :returns: The database's answer: Requested data / Failure message
        """
        print("Attempting request with data:", data)
        answer = self.database.get_user_attr(split_string(data))
        print("Database's answer:", answer)
        return answer

    def act_add_title(self, data):
        """
        Tries to add a given title to the database
        Returns a suitable message for success or failure
        :param data: [name, date, type, user]
        :returns: The database's answer: Added title / Failure message
        """
        print("Attempting to add title with data:", data)
        if not is_english(data):
            return "Search only works in english."
        data = split_string(data)
        title = self.database.search_title_data(data[:-1])
        if not isinstance(title, TitleClass):
            return title
        answer = self.database.add_title(title)
        if isinstance(answer, str):
            return answer
        request = join_arr(*answer, data[-1])
        answer = self.act_get_title(request)
        print("Database's answer:", answer)
        return answer

    def act_get_title(self, data):
        """
        Returns the requested title
        :param data: [type, imdb, user]
        :returns: Title with the given data / An error message
        """
        print("Attempting to get title with data:", data)
        data = split_string(data)
        record = self.database.get_title(data[:-1])
        if record == "Error":
            return "Error"
        record, link = record
        record += self.database.get_user_rating(data)
        bookmarks = self.database.access_bookmarks(["get", data[2], data[1]])
        record.append(bookmarks)

        lists = self.database.get_user_attr(["lists", data[2],
                                             data[1], "true"])
        record.append(lists)

        can_stream = data[1].split("/")[-1]
        if data[0] == "Movie":
            can_stream = str(can_stream in Stream.Files_dict)
        else:
            can_stream = str(can_stream in Stream.shows_dict)
        record.append(("can_stream", can_stream))
        separators = find_sep(str(record), maximum=2)
        new = []
        for i in range(len(record)):
            new.append(separators[1].join(record[i]))
        title = separators[0].join(new)
        title = "({0})({1}){0}".format(*separators[:2]) + title
        print("Database's answer:", title)
        return title

    def act_rate_title(self, data):
        """
        Rates a title
        :param data: An array containing the user who is rating the title,
                     the imdb link of the title, the type of the title
                     and the new rating of the user to the title
        :returns: Updated rating with a suitable message / An error message
        """
        print("Attempting to rate title with data:", data)
        answer = self.database.set_user_rating(split_string(data))
        if not isinstance(answer, str):
            answer = join_arr(*answer)
        print("Database's answer: ", answer)
        return answer

    def act_edit_lists(self, data):
        """
        Does a certain action on the lists of a user
        :param data: An array containing the email of the user and more data
                     which depends on the wished action to do on the lists
                     (See database_access.py for all states)
        :returns: Depends on the wished action that the user wants to do
                  on the lists (See database_access.py for all states)
        """
        print("Attempting to edit lists with data: ", data)
        answer = self.database.set_user_list(split_string(data))
        print("Database's answer:", answer)
        print(answer)
        return answer

    def act_bookmarks(self, data):
        """
        Does a certain action on the bookmarks of a user
        :param data: An array containing the email of the user and more data
                     which depends on the wished action to be done on
                     the bookmarks. (See database_access.py for all states)
        :returns: Depends on the wished action that the user wants to do
                  on the bookmarks (See database_access.py for all states)
        """
        print("Attempting to get bookmarks with data: ", data)
        answer = self.database.access_bookmarks(split_string(data))
        print("Database's answer:", answer)
        return answer

    def act_rated_titles(self, data):
        """
        Gets all the titles that the user rated
        organized by type of titles (Movies and shows) and the ratings
        :param data: Email of the user
        :returns: All of the titles that the user has rated / An error message
        """
        print("Attempting to get rated titles with data: ", data)
        answer = self.database.get_rated_titles(data)
        print("Database's answer:", answer)
        return answer

    def act_edge_ratings(self, data):
        """
        Returns all the titles with rating ordered by the rating
        :param data: Order of the rating (Either highest to lowest
                     or lowest to highest)
        :returns: All rated titles in rating order
        """
        print("Attempted to get titles ordered by score with data: ", data)
        answer = self.database.get_edge_rating(data)
        print("Database's answer:", answer)
        return answer

    def act_stream(self, data):
        """
        :param data: An array containing: Full imdb link of a title
                     and the user's email
        :returns: Streaming address of the title
        """
        print("Attempting to stream with data: ", data)

        data = split_string(data)
        imdb = data[0].split("/")[-1]
        if imdb in Stream.Files_dict:
            url = ":8080/Watch/" + imdb
            languages = Stream.Files_dict[imdb][1]

            data = ["languages", data[1]]
            user_preferences = self.database.get_user_attr(data)
            for language in user_preferences:
                if language in languages:
                    subtitles = self.act_subtitles(join_arr(imdb, language))
                    return join_arr(url, join_arr(*languages), subtitles)
            return join_arr(url, join_arr(*languages))

        return "Error: Video not found"

    def act_possible_streams(self, data):
        """
        Returns a list of all titles possible to be streamed
        :param data: Nothing
        :returns: A list of all titles which have a possibility to be streamed
        """
        # Data is nothing
        files = list(Stream.Files_dict.keys())
        files.extend(Stream.shows_dict.keys())
        for i in range(len(files)):
            files[i] = IMDB_LINK + files[i]

        titles = self.database.get_titles_imdb(files)
        answer = Database.match_lists_screen(titles, True)
        return answer

    def act_subtitles(self, data):
        """
        Returns subtitles to a movie in a given language
        :param data: An array containing the id of the movie and the
                     requested language for the subtitles
        :returns: The subtitles file
        """
        # data is [imdb[id], language]
        imdb, language = split_string(data)
        path = Stream.Files_dict[imdb][0] + "Subtitles\\" + language + ".srt"

        # Some languages have difference encodings
        try:
            with open(path) as f:
                subtitles = f.read()
            print("Normal encoding")
        except UnicodeDecodeError:
            try:
                with open(path, encoding="utf8") as f:
                    subtitles = f.read()
                    print("UTF-8 encoding")
            except UnicodeDecodeError:
                try:
                    with open(path, encoding="utf16") as f:
                        subtitles = f.read()
                        print("UTF-16 encoding")
                except UnicodeDecodeError:
                    with open(path, encoding="latin-1") as f:
                        subtitles = f.read()
                    print("Latin-1 encoding")
        return subtitles

    def act_episodes_guide(self, data):
        """
        Returns episodes guide of a given show
        :param data: imdb link of the show
        :returns: episodes guide
        """
        data = data.split("/")[-1]
        seasons = Stream.shows_dict.get(data, None)
        if not seasons:
            return "Error: Something bad happened"
        guide = []
        print("Seasons: ", seasons)
        zfill = max(2, len(seasons))
        for season in sorted(seasons.keys(), key=lambda x: int(x)):
            episodes = seasons[season] + []  # Prevents pointer
            # Last episode -> longest number
            zfill = max(zfill, len(episodes[-1]))

            season = str(int(season))
            episodes.insert(0, season)
            episodes = join_arr(*episodes)
            guide.append(episodes)
        guide.append(str(zfill))
        guide = join_arr(*guide)
        return guide

    def act_edit_user(self, data):
        """
        Edits the user's data
        State A:
            :param data: ["name", email, update name, password]

        State B:
            :param data: ["password", email, update password, old password]

        State C:
            :param data: ["language", email, updated language preference]
        :returns: A success message or an error message
        """
        print("Attempting to edit user with data", data)
        data = split_string(data)
        answer = self.database.edit_user(data)
        print("Database answer: " + answer)
        return answer

    def run(self, client_socket):
        """
        Runs the server for a single client
        :param client_socket: The socket of the client
        """
        self.lock.acquire()
        num = self.thread_counter
        self.thread_counter += 1
        self.lock.release()
        print("Starting thread:", num)
        condition = True
        try:
            msg = choice(whitespace)
            msg = "({}){}".format(msg, msg.join(ARRAY_SEPARATORS))
            self.send_client_response(client_socket, msg)
            while condition:
                request = self.receive_client_request(client_socket)
                data, sep = split_string(request, True)
                if not sep:
                    condition = False
                    continue

                action, data = data[0].upper(), data[1]
                print("Action: {}\t\tData: {}".format(action, data))
                try:
                    print("Attempting action")
                    response = Server.actions[action](self, data)
                    print("Succeed doing action")
                except RuntimeError:  # KeyError:
                    response = "Non-existing"
                    condition = False
                except UnicodeEncodeError:
                    response = "Unicode chars made the code suicide"
                print("Response:", response)
                if isinstance(response, list):
                    sep = find_sep(str(response), maximum=1)[0]
                    msg = sep.join(response)
                    self.send_client_response(client_socket, msg)
                else:
                    self.send_client_response(client_socket, response)
        except socket.error as e:
            print("Exception: {}\n\t\t{}".format(str(type(e)), e))
        print("Ending thread:", num)

    @staticmethod
    def inputs_loop(server):
        """
        Loop waiting for user inputs
        :param server: The Server object
        """
        while True:
            string = input(">").lower()
            print("INPUT: ", string)
            if string == "close":
                break
            elif string == "update":
                print("Files updated.")
                Stream.set_files()
        server.database.shut_down()
        server.email_server.quit()
        print("~~~Safe closing~~~")
        global stop_condition
        stop_condition = True

    @staticmethod
    def run_till_shutdown():
        """
        Receives clients and handles them until shutdown
        """
        global stop_condition

        s = Server(IP, SERVER_PORT)
        print(1)
        Thread(target=Server.inputs_loop, args=(s,)).start()

        # Streaming
        Stream.create()

        while not stop_condition:
            try:
                client_socket, address = s.socket.accept()
                print("Connected to: ", address)
                client_thread = Thread(target=s.run,
                                       args=(client_socket,))
                client_thread.start()
            except socket.error as msg:
                print("Error2: ", msg)
            except Exception as e:
                print("Error3: ", e)
                print(e.__traceback__)


if __name__ == "__main__":
    Server.run_till_shutdown()
