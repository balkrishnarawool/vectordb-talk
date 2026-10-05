### Useful Elasticsearch Commands
- Useful cURL commands:
  - To see all documents in the index (epic_comic_store_vector_index):
```
curl -X GET "http://localhost:9200/epic_comic_store_vector_index/_search?pretty&size=100" -H 'Content-Type: application/json' -d'
{
  "query": { "match_all": {} }
}
'
```
- To delete all documents from the index (epic_comic_store_vector_index):
```
curl -X POST "http://localhost:9200/epic_comic_store_vector_index/_delete_by_query" -H 'Content-Type: application/json' -d'
{
  "query": { "match_all": {} }
}
'
```
- To delete the index (epic_comic_store_vector_index)
  `curl -X DELETE "http://localhost:9200/epic_comic_store_vector_index"`
