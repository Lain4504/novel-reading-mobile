package com.miraimagiclab.novelreadingapp.di

import com.miraimagiclab.novelreadingapp.data.graphql.GraphQLBookService
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import io.lain4504.novelreadingapp.api.web.WebBookDataSource
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class WebBookDataSourceBindingModule {
    @Binds
    @IntoSet
    @Singleton
    abstract fun bindGraphQLBookService(
        graphQLBookService: GraphQLBookService
    ): WebBookDataSource
}
