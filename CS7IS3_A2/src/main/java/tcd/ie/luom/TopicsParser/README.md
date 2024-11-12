# Parser Structure

- `TopicsParser.java`
	- Main file, the parser
	- As a parser, it uses `parse` method to extract data from the topics file
	- Use `Topic.java` and `TopicsWrapper.java`
	- It uses `TopicsWrapper` to store raw parsed data and then clean those data to finally give a topics list

- `Topic.java`
	- A model to store one topic
	- It has members: `num`, `title`, `desc`, and `narr`

- `TopicsWrapper.java`
	- To store raw parsed topics
	- Use `Topic.java` to store raw data
