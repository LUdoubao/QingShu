package org.doubao.quote.service.duplicate.check;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.io.Serializable;
import java.util.Arrays;
import java.util.Set;

@Document(collection = "quote_db")
public class CitationDocument implements Serializable {
	private static final long serialVersionUID = 1L;
	@Id
	private String id;

	private String content;

	@Indexed
	private String author;

	private String source;

	@Indexed
	private long[] contentSimHash;

	@Indexed
	private Set<String> trigrams;

	@Indexed
	private boolean isOriginal;

	public CitationDocument() {}

	public CitationDocument(String content, String author, String source,
							long[] contentSimHash, Set<String> trigrams, boolean isOriginal) {
		this.content = content;
		this.author = author;
		this.source = source;
		this.contentSimHash = contentSimHash;
		this.trigrams = trigrams;
		this.isOriginal = isOriginal;
	}

	// Getters and setters
	public String getId() { return id; }
	public void setId(String id) { this.id = id; }
	public String getContent() { return content; }
	public void setContent(String content) { this.content = content; }
	public String getAuthor() { return author; }
	public void setAuthor(String author) { this.author = author; }
	public String getSource() { return source; }
	public void setSource(String source) { this.source = source; }

	public long[] getContentSimHash() {
		return contentSimHash;
	}

	public void setContentSimHash(long[] contentSimHash) {
		this.contentSimHash = contentSimHash;
	}

	public Set<String> getTrigrams() { return trigrams; }
	public void setTrigrams(Set<String> trigrams) { this.trigrams = trigrams; }
	public boolean isOriginal() { return isOriginal; }
	public void setOriginal(boolean original) { isOriginal = original; }

	@Override
	public String toString() {
		return "CitationDocument{" +
				"id='" + id + '\'' +
				", content='" + content + '\'' +
				", author='" + author + '\'' +
				", source='" + source + '\'' +
				", contentSimHash=" + Arrays.toString(contentSimHash) +
				", trigrams=" + trigrams +
				", isOriginal=" + isOriginal +
				'}';
	}
}
