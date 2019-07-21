"""
Title class
"""
from pprint import pprint
from string import capwords

import imdb_read as imdb
from youtube_read import get_trailer_link
from values import IMDB_TERMS
TABLES = {"Movie": "Movies ", "Series": "Shows "}
TYPES = {"Movie": "Movie", "Series": "Show"}


class Title:
    """
    Represents a title
    """
    def __init__(self, title, year, title_type):
        """
        :param title: Name of the title
        :param title_type: type of the title
        :param year: Release date of the titles
        """
        print("ADDING TITLE:", title)
        if len(year) == 4:
            year += "-01-01"
        print(title)
        print(year)
        print(title_type)
        self.error_flag = False
        self.table = TABLES[title_type]
        self.type = TYPES[title_type]
        print("TYPE:", self.type)
        title, year, self.imdb_page = imdb.get_imdb_title(title, self.type, year)
        print("New title:", title)
        print("New year:", year)
        if self.imdb_page == "Failure":
            print("oof")
            self.error_flag = True
        if not self.error_flag:
            self.attributes = {
                "name": capwords(title),
                "imdb": self.imdb_page
            }
        if self.type == "Movie":
            self.attributes["trailer"] = get_trailer_link(title, self.type, year)

    def get_necessities(self):
        """
        :return: Gets all necessary things
        """
        print("IMDB - necessities: needs:", IMDB_TERMS)
        imdb_nec = imdb.get_necessities(self.imdb_page, IMDB_TERMS + [], self.type)
        self.attributes.update(imdb_nec)

    def ready_to_database(self):
        """
        Database record should look like:
        ('name', 'date', 'poster', 'parents guide','length', 'genres',
         'plot', 'language', 'actors', 'directors', 'producers', 'writers')
        :return:
        """
        pprint(self.attributes, indent=4)
        keys = "({})".format(", ".join(self.attributes.keys()))
        values = list(self.attributes.values())
        empty = "({})".format(", ".join(["?"] * len(values)))
        string = self.table + keys + " VALUES" + empty
        return string, values
