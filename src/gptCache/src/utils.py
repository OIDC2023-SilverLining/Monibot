import logging

def get_logger(logger_name, file_name, logging_level=logging.INFO):
    formatter = logging.Formatter("[%(asctime)s] %(levelname)s %(message)s")
    handler = logging.FileHandler(file_name)
    handler.setFormatter(formatter)
    logger = logging.getLogger(logger_name)
    logger.setLevel(logging_level)
    logger.addHandler(handler)
    return logger