import Html exposing (..)
import Html.Attributes exposing (..)
import Html.Events exposing (..)
import Http
import Json.Decode exposing (..)
import Random
import Platform

main =
  Html.program
    { init = init ""
    , view = view
    , update = update
    , subscriptions = subscriptions
    }


-- MODEL
type alias CountryInfo =
  { name: String
  }
type alias MainWeather =
  { temp: Float
  }
type alias WeatherInfo =
  { main: MainWeather
  }
type alias Model =
  { topic: String
  , city: String
  , countryInfo: CountryInfo
  , weather: WeatherInfo
  }


init : String -> (Model, Cmd Msg)
init city =
  ( Model city city (CountryInfo "") (WeatherInfo (MainWeather 0.0))
  , fetch city
  )


-- UPDATE
type Msg
  = Default
  | More String
  | NewModel (Result Http.Error Model)

update : Msg -> Model -> (Model, Cmd Msg)
update msg model =
  case msg of
    Default ->
      (model, fetch "")

    More city ->
      (model, fetch city)

    NewModel (Ok newModel) ->
      (newModel, Cmd.none)

    NewModel (Err _) ->
      (model, Cmd.none)


-- VIEW
view : Model -> Html Msg
view model =
  div [
    style [
      ("text-align", "center")
    ]
  ] [
    button [ onClick Default ] [ text "Default" ]
    , input [ placeholder "city", onInput More ] []
    , br [] []
    , h2 [] [text model.city]
    , h5 [] [text model.countryInfo.name]
    , h5 [] [text (toString model.weather.main.temp ++ " ˚C")]
  ]


-- SUBSCRIPTIONS
subscriptions : Model -> Sub Msg
subscriptions model =
  Sub.none


-- HTTP
fetch : String -> Cmd Msg
fetch city =
  let
    url = "api/" ++ city
  in
    Http.send NewModel (Http.get url infoDecoder)

mainWeatherInfoDecoder = Json.Decode.map MainWeather (field "temp" float)
weatherInfoDecoder = Json.Decode.map WeatherInfo (field "main" mainWeatherInfoDecoder)
countryInfoDecoder = Json.Decode.map CountryInfo (field "name" string)
infoDecoder = map4 Model
  (field "city" string)
  (field "city" string)
  (field "countryInfo" countryInfoDecoder)
  (field "weatherInfo" weatherInfoDecoder)
