package models.github.put

import models.github.RepoContentItem
import play.api.libs.json.{Json, OFormat}

case class PutResponse(
                        content: RepoContentItem
                      )

object PutResponse{
  implicit val formats: OFormat[PutResponse] = Json.format[PutResponse]
}

/**
 * {
 * "content": {
   * "name": "hello.txt",
   * "path": "notes/hello.txt",
   * "sha": "a56507ed892d05a37c6d6128c260937ea4d287bd",
   * "size": 9,
   * "url": "https://api.github.com/repos/octocat/Hello-World/contents/notes/hello.txt",
   * "html_url": "https://github.com/octocat/Hello-World/blob/master/notes/hello.txt",
   * "git_url": "https://api.github.com/repos/octocat/Hello-World/git/blobs/a56507ed892d05a37c6d6128c260937ea4d287bd",
   * "download_url": "https://raw.githubusercontent.com/octocat/HelloWorld/master/notes/hello.txt",
   * "type": "file",
   * "_links": {
     * "self": "https://api.github.com/repos/octocat/Hello-World/contents/notes/hello.txt",
     * "git": "https://api.github.com/repos/octocat/Hello-World/git/blobs/a56507ed892d05a37c6d6128c260937ea4d287bd",
     * "html": "https://github.com/octocat/Hello-World/blob/master/notes/hello.txt"
    * }
 * },
 * "commit": {
   * "sha": "18a43cd8e1e3a79c786e3d808a73d23b6d212b16",
   * "node_id": "MDY6Q29tbWl0MThhNDNjZDhlMWUzYTc5Yzc4NmUzZDgwOGE3M2QyM2I2ZDIxMmIxNg==",
   * "url": "https://api.github.com/repos/octocat/Hello-World/git/commits/18a43cd8e1e3a79c786e3d808a73d23b6d212b16",
   * "html_url": "https://github.com/octocat/Hello-World/git/commit/18a43cd8e1e3a79c786e3d808a73d23b6d212b16",
   * "author": {
     * "date": "2014-11-07T22:01:45Z",
     * "name": "Monalisa Octocat",
     * "email": "octocat@github.com"
   * },
   * "committer": {
     * "date": "2014-11-07T22:01:45Z",
     * "name": "Monalisa Octocat",
     * "email": "octocat@github.com"
   * },
   * "message": "my commit message",
   * "tree": {
     * "url": "https://api.github.com/repos/octocat/Hello-World/git/trees/9a21f8e2018f42ffcf369b24d2cd20bc25c9e66f",
     * "sha": "9a21f8e2018f42ffcf369b24d2cd20bc25c9e66f"
   * },
   * "parents": [
       * {
       * "url": "https://api.github.com/repos/octocat/Hello-World/git/commits/da5a433788da5c255edad7979b328b67d79f53f6",
       * "html_url": "https://github.com/octocat/Hello-World/git/commit/da5a433788da5c255edad7979b328b67d79f53f6",
       * "sha": "da5a433788da5c255edad7979b328b67d79f53f6"
     * }
   * ],
   * "verification": {
     * "verified": false,
     * "reason": "unsigned",
     * "signature": null,
     * "payload": null
   * }
 * }
 * }
 */