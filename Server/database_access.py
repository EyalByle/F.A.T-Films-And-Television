"""
Database thingy
"""
import sqlite3

from title import Title
from values import is_float, split_string, find_sep, gen_pin_code
from values import TITLE_NUMBERS
TYPE_TO_TABLE = {"Movie": "Movies", "Series": "Shows"}
MAX_RATING = 10
LISTS_LIMIT = 100


class Database:
    """
    Class that represents the database and accesses it
    """

    # Easier for searching stuff
    Selector = """SELECT %s FROM {}
    WHERE imdb IN %s ORDER BY name ASC"""

    # Formatting options in Selector
    Options = {
        "Poster": "poster",
        "Normal": "name, date, poster, imdb, rating"
    }

    def __init__(self):
        """
        Initiates the database
        """
        self.connection = sqlite3.connect("Databases.db",
                                          check_same_thread=False)
        self.cursor = self.connection.cursor()

    def app_email_login(self):
        """
        Takes the email login of the app from the database and returns it
        :returns: email and a password
        """
        try:
            email, password = list(self.cursor.execute("""SELECT
            email, password FROM Users WHERE id=0"""))[0]
        except IndexError:
            return None, None
        return email, password

    @staticmethod
    def injection_protection(string: str):
        string = string.replace("\"", "\"\"")
        string = string.replace("\'", "\'\'")
        return string

    @staticmethod
    def injection_arr(array):
        return [Database.injection_protection(string) for string in array]

    def register(self, data):
        """
        Adds a user to the database
        :param data: [name, email, password, birthday]
        :returns: Success and pin code or failure message and 0
        """
        try:
            pin = gen_pin_code()
            data.append(pin)
            data = Database.injection_arr(data)
            data = repr(tuple(data))
            request = """INSERT INTO Users(name, email,
            password, birthday, activated)"""
            request += " VALUES" + data
            self.cursor.execute(request)
            self.connection.commit()
            print("Success!")
        except sqlite3.IntegrityError as e:
            print("Something bad happened: \t\t", str(e))
            return "Something bad happened", 0
        except sqlite3.InterfaceError:
            print("Whoopsie, that is not supposed to happen")
            return "Exists", 0
        except TypeError:
            return "Developers did something wrong", 0
        return "SUCCESS", pin

    def activate_user(self, data):
        """
        Tries to activate user with a given activation key
        :param data: [email, activation key]
        :returns: Whether or not it worked
        """
        if len(data) != 2:
            print("WE ARE GOING DOWN PRIVATE: ", data)
            return "Error: Something went wrong"
        data = Database.injection_arr(data)
        user = list(self.cursor.execute("""SELECT activated FROM Users
        WHERE email = \"{}\"""".format(data[0])))
        print(user)

        if not user:
            message = "Error: No user with the email address: \"{}\" found"
            message = message.format(data[0])
        elif str(user[0][0]) != data[1]:
            message = "Error: Bad PIN code"
        else:
            data[1] = 0
            self.cursor.execute("""UPDATE Users SET activated = {}
            WHERE email = \"{}\"""".format(*data[::-1]))
            self.connection.commit()
            message = "Success"
        return message

    def delete_user(self, email):
        """
        Completely deletes a user from the database
        (Case of non-existing email address)
        :param email: Email of the user
        """
        email = Database.injection_protection(email)
        query = "DELETE FROM Users WHERE email=\"{}\"".format(email)
        self.cursor.execute(query)
        self.connection.commit()

    def check_user_login(self, email, password):
        """
        Returns whether or not the user exists and activated
        :param email: Email address of the user
        :param password: Password of the user
        :returns: a tuple of the user's Username,
                  the stream requests that the user made
                  and the user's preferred languages for subtitles
                  or reason for failed login
        """
        email = Database.injection_protection(email)
        query = """SELECT name, password, activated, stream_requests, languages
        FROM Users WHERE email = \"{}\"""".format(email)
        user = list(self.cursor.execute(query))
        print(user)
        print("{}:::{}".format(email, password))
        if not len(user):  # User does not exist
            return "Does not exist"
        user = user[0]
        if user[1] != password:  # Wrong password
            return "Wrong password"
        if user[2]:  # User didn't activate the account
            return "ACTIVATE"
        return user[0], user[-2], user[-1]

    @staticmethod
    def match_lists_screen(records, triple_nest=False):
        """
        Joins a 2/3 dimensional list to a single string
        :param records: 2/3 Dimensional list
        :param triple_nest: Whether the list is 2d or 3d
        :returns: A string
        """
        answer = []
        for titles in records:
            condition = True
            separators = find_sep(str(titles), maximum=2)
            new = []
            for i in range(len(titles)):
                # print("[{}]: {}".format(i, str(titles[i])))
                if not isinstance(titles[i], str):
                    condition = False
                    new.append(separators[1].join(titles[i]))
            if condition:
                joined = "({0})".format(separators[0])
                joined += separators[0].join(titles)
                answer.append(joined)
            else:
                titles = separators[0].join(new)
                answer.append("({0})({1}){0}".format(*separators[:2]) + titles)
        if isinstance(answer, list):
            if triple_nest:
                sep = find_sep(str(answer), True, 1)[0]
                answer = "({})".format(sep) + sep.join(answer)
            else:
                sep = find_sep(str(answer), maximum=1)[0]
                answer = "({})".format(sep) + sep.join(answer)
        return answer

    @staticmethod
    def transform_rating(titles, single=False):
        """
        Transforms the bad looking rating of all of the given titles
        to less ugly
        :param titles: list of titles
        :param single: Whether its multiple titles or a single one
        :returns: [name, date, poster, imdb, avg_rating]
        """

        if single:
            titles = [titles]

        for i in range(len(titles)):
            titles[i] = list(titles[i])
            print("Title:", titles[i])
            for j in range(len(titles[i])):
                print(titles[i][j])
                t = titles[i][j]

                if isinstance(t, (tuple, list)):
                    t = list(t)
                    t[-1] = round(t[-1], 1)
                    if t[-1] == int(t[-1]):
                        t[-1] = int(t[-1])
                    if not t[-1]:
                        t[-1] = "N/A"
                    t[-1] = str(t[-1])
                elif isinstance(t, float):
                    if t:
                        t = round(t, 1)
                        if t == int(t):
                            t = int(t)
                        t = str(t)
                    else:
                        t = "N/A"
                titles[i][j] = t
        return titles

    def search_title(self, data):
        """
        Searches a title in the database by name
        It's in an array form to enable advanced search option in the future
        :param data: [search term]
        :returns: Search results as a string
        """
        for i in range(min(2, len(data))):
            data[i] = "\"%{}%\"".format(Database.injection_protection(data[i]))
        query = ["SELECT", Database.Options["Normal"], "FROM", "WHERE"]
        search = zip(["name", "date"], data)
        query.append(" AND ".join(" LIKE ".join(x) for x in search))
        query.append("ORDER BY name ASC")
        query2 = query.copy()
        query.insert(3, "Movies")
        query2.insert(3, "Shows")
        results = list()
        results.append(list(self.cursor.execute(" ".join(query))))
        results.append(list(self.cursor.execute(" ".join(query2))))
        if not any(results):
            return "Error"
        print(results)
        for i in range(len(results)):
            results[i] = Database.transform_rating(results[i])
        answer = Database.match_lists_screen(results, True)
        return answer

    @staticmethod
    def translate_command(command, nested, title_type, ret_link=False):
        """
        Gets a command and a bit more data
        returns a list with all the command's data
        :param command: Cursor execution
        :param nested: Whether or not should it nest it
        :param title_type: Movie / Show
        :param ret_link: Whether or not should it return the imdb link
        :returns: The command as a 1 or 2 dimensional list (depends on nested)
        """
        records = list(zip(*list(command)))
        record = dict(zip([x[0] for x in command.description],
                          [r[0] for r in records]))
        record["type"] = title_type
        if not len(record):
            return "Error"
        answer = []
        print("Record:", record)
        for k, v in record.items():
            print(k, v)
            if isinstance(v, (int, float)):
                if k not in TITLE_NUMBERS:
                    continue
                v = round(v, 1)
                if v == int(v):
                    v = int(v)
                if not v:
                    v = "N/A"
                v = str(v)
            if nested:
                answer.append((k, v))
            else:
                answer.extend((k, v))
        if ret_link:
            return answer, record["imdb"]
        return answer

    def get_title(self, data):
        """
        Gets the title's type and its imdb link and returns the title
        :param data: [title's type, imdb]
        :returns: A title
        """
        query = list(["SELECT", "*", "FROM", data[0] + "s", "WHERE"])
        query.append("imdb LIKE \"{}\"".format(data[1]))
        command = self.cursor.execute(" ".join(query))
        answer, imdb = Database.translate_command(command, True, data[0], True)
        if isinstance(answer, str):
            return "Error"
        print("Found title: ", answer)
        return answer, imdb

    def get_titles_imdb(self, imdb_list, mode="Normal"):
        """
        Gets a list of imdb links and returns the corresponding titles
        in the give view mode
        :param imdb_list: list of imdb links
        :param mode: view mode (what should be taken for each title)
        :returns: A list of titles
        """

        if not imdb_list:
            return []
        if len(imdb_list) == 1:
            imdb_list = "(\"{}\")".format(imdb_list[0])
        else:
            imdb_list = repr(tuple(imdb_list))
        if mode not in Database.Options:
            mode = "Normal"
        takes = Database.Options[mode]
        request = Database.Selector % (takes, imdb_list)
        titles = list()
        titles.append(list(self.cursor.execute(request.format("Movies"))))
        titles.append(list(self.cursor.execute(request.format("Shows"))))

        if mode == "Normal":
            print("Normalizing")
            for i in range(len(titles)):
                titles[i] = Database.transform_rating(titles[i])
        return titles

    def return_proper_lists(self, user_list):
        """
        Gets a user list and returns a corresponding list of titles
        :param user_list: list of imdb links
        :returns: titles list
        """
        length = len(user_list)
        if not length:
            return ["0 Items", "DEFAULT", "Empty"]
        length = str(length) + " Items"
        posters = self.get_titles_imdb(user_list, "Poster")
        posters = sum(map(list, posters), [])
        return [length, posters[0][0], "Empty"]

    def get_user_attr(self, data):
        """
        Gets a specific attribute of a user from the database

        A:  Admin state
                :param data: [request, User mail]
                :returns: if the user is an admin

        B:  Adds a given title to the user's stream requests
                :param data: [request, User email, imdb link]
                :returns: success / failure message

        C: Returns the user's subtitles languages
                :param data: [request, User email]
                :returns: Array of the user's languages preferences

        D:  Returns all of the titles in a given list of the user
                :param data: [request, User mail, list_name]
                :returns: items in user list

        E:  Returns all of the lists of a user and a boolean for each list
            Whether or not the given title is in it
                :param data: [request, User mail, imdb_link, true]
                :returns: user lists and title in lists

        F:  Returns all of the lists of a user
            and how many titles are in each list
                :param data: [request, User mail, Useless, false]
                :returns: user lists and items count
        I probably should change the way it works
        """
        print("In attr")
        data[1] = Database.injection_protection(data[1])
        if data[0] == "rating":
            return self.get_user_rating(data[1:])
        attr = list(self.cursor.execute("""SELECT {} FROM Users
        WHERE email=\'{}\'""".format(*data[:2])))[0][0]
        print("attr:", repr(attr))

        # State A
        if data[0] == "admin":
            return str(bool(attr))

        # State B
        elif data[0] == "stream_requests":
            try:
                attr = eval(attr)
                attr.append(data[2])
                query = """UPDATE Users SET stream_requests=\"{}\"
                WHERE email=\"{}\"""".format(str(attr), data[1])
                self.cursor.execute(query)
                self.connection.commit()
                return "Success"
            except Exception as e:
                print(str(e))
                return "Developer did a whoopsie"

        # State C
        elif data[0] == "languages":
            return eval(attr)

        elif data[0] == "lists":
            print("Entered lists")

            # States E and F
            if len(data) == 4:
                flag = data[3] == "true"
                lists = eval(attr)
                keys = list(lists.keys())
                if keys:
                    for i in range(len(keys)):
                        key = keys[i]
                        keys[i] = [key]
                        if flag:  # State E
                            keys[i].append(str(bool(data[2] in lists[key])))
                        else:  # State F
                            temp = self.return_proper_lists(lists[key])
                            keys[i].extend(temp)
                else:
                    return "Error(Empty): No lists exist"
                return ["lists", Database.match_lists_screen(keys)]

            # State D
            return self.get_user_list(attr, data[2])

    def access_bookmarks(self, data):
        """
        Does something concerning the User's bookmarks list
        The output depends on the parameter data, which is an array

        State A:
            Gets all of the user's bookmarks list
            :param data: ["get", User.email]
            :returns: All the titles in the user's bookmarks

        State B:
            Checks if a given title is in the user's bookmarks
            :param data: ["get", User.email, imdb]
            :returns: Whether or not the given title[imdb]
                      is in the user's bookmarks list

        State C:
            Adds or removes a given title to the user's bookmarks
            :param data ["set", User.email, action("add" or "remove"), imdb]
            :returns: Either a success message or an error message
        """
        data[1] = Database.injection_protection(data[1])
        bookmarks = list(self.cursor.execute("""SELECT bookmarks FROM Users
        WHERE email=\"{}\"""".format(data[1])))[0][0]
        bookmarks = eval(bookmarks)
        print("Bookmarks:", bookmarks)

        # State A / State B
        if data[0] == "get":
            if not bookmarks:
                return "Error: Literally nothing to see here"
            # State A
            if len(data) == 2:
                titles = self.get_titles_imdb(bookmarks)
                answer = Database.match_lists_screen(titles, True)
                return answer
            # State B
            return ["bookmark", str(data[2] in bookmarks)]
        # State C
        elif data[0] == "set":
            if len(data) != 4:
                return "Error: Length Error?"
            if not bookmarks and data[2] == "false":
                return "Error: Um, Error?"
            bookmarks = set(bookmarks)
            if data[2] == "false":
                bookmarks.remove(data[-1])
            else:
                bookmarks.add(data[-1])
            bookmarks = repr(str(list(bookmarks)))
            self.cursor.execute("""UPDATE Users SET bookmarks = {}
            WHERE email = \"{}\"""".format(bookmarks, data[1]))
            self.connection.commit()
            return "Success(?)"
        return "Error: Unidentified statement"

    def get_user_list(self, lists, list_name):
        """
        :param lists: A repr of a dict containing all of the user's lists
                      Each user list is a list of imdb links
        :param list_name: Name of the list
        :returns: ListScreen representation of the user list (if list exists)
                 Or a fitting error message for other cases
        """
        print("Entering get_user_list with lists:", lists)
        print("and with name:", list_name)
        lists = eval(lists)
        if list_name not in lists:
            return "Error: List does not exist"
        titles = self.get_titles_imdb(tuple(lists[list_name]))
        if not titles:
            return "Error: List is empty"

        answer = Database.match_lists_screen(titles, True)
        print("ANSWER AFTER MATCH: ", answer)
        return answer

    def set_user_list(self, data):
        """
        Changes value for something inside of the user's lists
        The change depends on the array parameter 'data'

        State A:
            Gets [User email, lists to add a title to,
                 lists to remove a title from, title imdb list]
            Adds the given title to given lists and removes the given title
                From given lists
            :param data: [User mail, add_to_lists,
                          delete_from_lists, imdb_link]
            :returns: Either an error message or updated user lists
                      with items count (See get_user_attr - State E)

        State B:
            Gets [User email, the word "create" and a string
                  that represents a list's name]
            Tries to add the list to the user's lists
            :param data: [User mail, "create", list_name]
            :returns: Either an error message or updated user lists
                      with items count (See get_user_attr - State E)

        State C:
            Gets (User email, the word "delete" and an array of strings
                  Each string is the name of a user's list)
            :param data: [User mail, "delete", lists]
            :returns: Either an error message or updated user lists
                      with items count (See get_user_attr - State E)
        """
        data = Database.injection_arr(data)
        print("Sets user list with data: ", data)
        if len(data) < 3:
            return "Error: Developers still cant code"
        lists = list(self.cursor.execute("""SELECT lists FROM Users
        WHERE email = \"{}\"""".format(data[0])))
        lists = eval(lists[0][0])
        for list_name in lists:
            lists[list_name] = set(lists[list_name])

        # State B / State C
        if data[1] in ("create", "delete"):
            if len(data) > 3:
                return "Error: Failed randomly, as usual"

            # State B
            if data[1] == "create":
                if data[2] in lists:
                    return "Error: Could not create an existing list"
                lists[data[2]] = []

            # State C
            elif data[1] == "delete":
                names = split_string(data[2])
                for name in names:
                    if name not in lists:
                        return "Error: Could not remove a non-existing entry"
                    lists.pop(name)
            print("Success?\tCREATE_DELETE")

        # State A
        elif len(data) == 4:
            data[1] = split_string(data[1])
            data[2] = split_string(data[2])
            imdb = data[-1]
            for list_name in data[1]:
                print("NAME: ", list_name)
                if list_name:
                    lists[list_name].add(imdb)
            for list_name in data[2]:
                print("NAME: ", list_name)
                if list_name:
                    try:
                        lists[list_name].remove(imdb)
                    except KeyError:
                        print("Error or something", lists, list_name)
        else:
            return "Error: Non-existing request"
        for list_name in lists:
            lists[list_name] = list(lists[list_name])
        self.cursor.execute("""UPDATE Users SET lists = {}
        WHERE email = \"{}\"""".format(repr(str(lists)), data[0]))
        self.connection.commit()
        return self.get_user_attr(["lists", data[0], "Useless", "false"])

    def get_user_rating(self, data):
        """
        Gets the rating of the user to a given title
        from the database if possible
        :param data: [type, imdb link, User mail]
                     Type: Type of the title
                     User mail: Email of the user
                     imdb link: Imdb link of the title
        :returns: User rating (getting rating is possible)
                 or an empty array (getting rating is impossible)
        """

        print(data)
        data[2] = Database.injection_protection(data[2])
        rating = list(self.cursor.execute("""SELECT rated_{}s FROM Users
        WHERE email=\'{}\'""".format(data[0], data[2])))
        rating = eval(rating[0][0])
        key = data[1]
        if key not in rating:
            return []
        return ("user_rating", str(rating[key])),

    def set_user_rating(self, data):
        """
        Changes user rating of a given title
        :param data: [type, imdb link, User mail, new]
                     Type: Type of the title
                     User mail: Email of the user
                     imdb link: Imdb link of the title
                     New: New rating
        :returns: Updated rating with a suitable message if succeed
                  Error message if the code failed for any reason
        """
        if not (len(data) == 4 and is_float(data[-1])):
            print("DATA:", data)
            return "Error: Developers cannot 'code' properly."
        data[1] = Database.injection_protection(data[1])
        rating = eval(list(self.cursor.execute("""SELECT rated_{}s FROM Users
        WHERE email=\'{}\'""".format(data[0], data[1])))[0][0])
        title_imdb = data[2]
        count = TITLE_NUMBERS["rating"]
        rating[title_imdb] = rating.get(title_imdb, 0)
        old, new = rating[title_imdb], int(float(data[-1]))
        rating[title_imdb] = new
        request = """UPDATE {0}s SET
        {1} = ({1} * {2} - {3} + {4}) / ({2} + {5}), {2} = {2} + {5}
        WHERE imdb = \"{6}\""""
        request = request.format(data[0], "rating", count,
                                 old, new, int(old == 0), title_imdb)
        print(rating)
        print("REQUEST:", request)
        self.cursor.execute(request)
        self.cursor.execute("""UPDATE Users SET rated_{}s = \"{}\"
        WHERE email=\"{}\"""".format(data[0], str(rating), data[1]))
        self.connection.commit()
        new = list(self.cursor.execute("""SELECT rating FROM {}s
        WHERE imdb=\"{}\"""".format(data[0], title_imdb)))[0][0]
        new = round(float(new), 1)
        if new == int(new):
            new = int(new)
        return str(new), "You have successfully rated this title."

    def search_title_data(self, data):
        """
        Sort of a sub-method of add_title
        Scrapes pre-chosen websites for info about a title with
        the given details (name, year, type)
        :param data: (Title's name, Title's year, Title's Type)
                      Name: Name of the title
                      Year: Release year of the title (minimum search year)
                      Type: Type of the title

        Success:
            :returns: A title with all the data scraped online
            :rtype: Title
        Failure:
            :returns: Error message (Reason for failure)
            :rtype: str
        """
        data = Database.injection_arr(data)
        print("Adding title\n\tName: {}\n\tYear: {}\n\tType: {}".format(*data))
        data[0] = data[0].lower()
        title_type = data[2]
        titles = dict(self.cursor.execute("""SELECT name,
        date FROM {}""".format(TYPE_TO_TABLE[title_type])))
        for title, year in titles.items():
            if data[0] == title.lower() and data[1] == year.split("-")[0]:
                return "Exists"
        title = Title(*data)
        if title.error_flag:
            return "Error"
        links = list(self.cursor.execute("""SELECT
        imdb FROM {}""".format(TYPE_TO_TABLE[title_type])))
        links = [link[0] for link in links]  # That's just how it gets stuff
        print("LINKS:", repr(links))
        print("IMDB:", repr(title.imdb_page))
        if title.imdb_page in links:
            return "Exists"
        return title

    def add_title(self, title):
        """
        Tries to add a given title to the Database
        :param title: A title containing all of the scraped data
                      about an already searched title
        :type title: Title

        Success:
            :returns: A tuple containing the type of the title
                      and the imdb link of the title
            :rtype: tuple

        Failure:
            :returns: The word "Error"
            :rtype: str
        """
        title.get_necessities()
        st, data = title.ready_to_database()
        st = """INSERT INTO {}""".format(st)
        print(st)
        for i, d in enumerate(data):
            print(i, repr(d), type(d))
        try:
            self.cursor.execute(st, data)
            self.connection.commit()
        except sqlite3.Error as e:
            print(str(e))
            return "Error"
        return title.type, title.imdb_page

    def get_rated_titles(self, email):
        """
        Gets an email of a user and returns a ListScreen representation
        of them all
        :param email: User.email
        :returns: ListScreen representation of all the rated titles of the user
        :rtype: str
        """
        email = Database.injection_protection(email)
        request = """SELECT rated_movies, rated_shows FROM Users
        WHERE email = \"{}\"""".format(email)
        print(request)
        user_rating = list(self.cursor.execute(request))[0]
        print(user_rating)
        user_rating = [eval(x) for x in user_rating]
        if not any(user_rating):
            print("Everything is empty")
            return "Error: Nothing rated yet"
        rated = {}
        for lists in user_rating:
            for imdb, rating in lists.items():
                rated[rating] = rated.get(rating, [])
                rated[rating].append(imdb)
        answer = []
        for key in sorted(rated.keys(), key=lambda x: -x):
            rated[key] = self.get_titles_imdb(rated[key])
            list_screen = self.match_lists_screen(rated[key])
            answer.append("[{}]".format(key) + list_screen)
        sep = find_sep(str(answer), True, 1)[0]
        result = "({}){}".format(sep, sep.join(answer))
        print(result)
        return result

    def get_edge_rating(self, order):
        """
        Gets a number (string) and returns a list of all titles with ratings
        sorted from one edge rating to the other.
        Edge ratings are top rating (highest rated title)
        and bottom rating (lowest rated title)
        :param order: Number in a string form representing the order style
            If the number is positive:
                Returns all titles with ratings sorted in descending style
                (from highest rated to lowest rated)
            If the number is positive:
                Returns all titles with ratings sorted in ascending style
                (from lowest rated to highest rated)
        :type order: str
        :returns: ListScreen representation of the titles ordered
                  by the given order style
        """
        # If order < 0, its a ascending order -> bottom rated
        # else its top rated
        order_by = "ORDER BY rating {}, raters_count DESC"
        if order.startswith("-"):
            order_by = order_by.format("ASC")
        else:
            order_by = order_by.format("DESC")
        query = """SELECT %s FROM {}
        WHERE rating > 0 %s""" % (Database.Options["Normal"], order_by)
        print(query)
        titles = list()
        titles.append(list(self.cursor.execute(query.format("Movies"))))
        titles.append(list(self.cursor.execute(query.format("Shows"))))
        if not any(titles):
            return "Error: Something went horribly wrong"
        titles = Database.transform_rating(titles)
        print(titles)
        answer = Database.match_lists_screen(titles, True)
        print(answer)
        return answer

    def edit_user(self, data):
        """

        :param data:
        :returns:
        """
        data = Database.injection_arr(data)
        if len(data) == 3 and data[0] == "languages":
            data[2] = repr(split_string(data[2]))
            print("DATA: ", data)
            self.cursor.execute("""UPDATE Users SET languages=\"{}\"
            WHERE email=\"{}\"""".format(*data[:0:-1]))
            self.connection.commit()

        elif len(data) == 4:
            user = list(self.cursor.execute("""SELECT name, password FROM Users
            WHERE email=\"{}\"""".format(data[1])))[0]
            if user[1] != data[-1]:
                return "Bad password"
            if data[0] == "name":
                if data[2] == user[0]:
                    return "You can't change your username to your current one"
            elif data[0] == "password":
                if data[2] == user[1]:
                    return "You can't change your password to your current one"
            else:
                return "An error has occurred"
            query = """UPDATE Users SET {0}=\"{2}\"
            WHERE email=\"{1}\"""".format(*data[:-1])
            print(query)
            self.cursor.execute(query)
            self.connection.commit()
        else:
            return "Non existing request"
        return "SUCCESS"

    def shut_down(self):
        """
        Shuts down the DatabaseAccess object by closing the database connection
        """
        self.connection.close()
