package com.miraimagiclab.novelreadingapp.network

object GraphQLQueries {
    val GET_NOVEL = """
        query GetNovel(${'$'}id: ID!) {
            novel(id: ${'$'}id) {
                id
                title
                slug
                description
                coverImage
                largeBackground
                categories
                tags
                viewCount
                followCount
                rating
                ratingCount
                wordCount
                chapterCount
                authorId
                authorName
                illustratorName
                status
                type
                createdAt
                updatedAt
                isR18
            }
        }
    """
    
    val GET_VOLUMES = """
        query GetVolumes(${'$'}novelId: ID!) {
            volumes(novelId: ${'$'}novelId) {
                id
                title
                novelId
                coverImage
                volumeNumber
                chapterCount
                description
                createdAt
                updatedAt
            }
        }
    """
    
    val GET_CHAPTERS = """
        query GetChapters(${'$'}volumeId: ID!) {
            chapters(volumeId: ${'$'}volumeId) {
                id
                title
                volumeId
                novelId
                chapterNumber
                wordCount
                viewCount
                status
                createdAt
                updatedAt
                previousChapterId
                nextChapterId
            }
        }
    """
    
    val GET_CHAPTER = """
        query GetChapter(${'$'}id: ID!) {
            chapter(id: ${'$'}id) {
                id
                title
                content
                volumeId
                novelId
                chapterNumber
                wordCount
                viewCount
                status
                createdAt
                updatedAt
                previousChapterId
                nextChapterId
            }
        }
    """
    
    val SEARCH_NOVELS = """
        query SearchNovels(${'$'}title: String!) {
            searchNovels(title: ${'$'}title) {
                id
                title
                slug
                description
                coverImage
                authorName
                categories
                tags
                viewCount
                followCount
                rating
                ratingCount
                wordCount
                chapterCount
                status
                type
                isR18
            }
        }
    """
}

