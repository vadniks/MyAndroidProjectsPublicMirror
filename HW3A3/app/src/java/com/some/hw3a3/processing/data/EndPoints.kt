package com.some.hw3a3.processing.data

import com.some.hw3a3.processing.model.GithubUser
import com.some.hw3a3.processing.model.Repo
import io.reactivex.Flowable
import io.reactivex.Observable
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * @author Vad Nik.
 * @version dated Sep 14, 2018.
 * @link http://github.com/vadniks
 */
interface EndPoints {

    @GET("/users/{user}")
    fun getUser(@Path("user") user: String): Observable<GithubUser>

    @GET("/users/{user}/repos")
    fun getRepos(@Path("user") user: String): Flowable<ArrayList<Repo>>
}
