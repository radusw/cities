var app = new Vue({
    el: '#app',
    data: {
        city: 'Bucharest',
        model: {
            countryInfo: {
                name: ''
            },
            weatherInfo: {
                main: {
                    temp: ''
                }
            }
        }
    },
    methods: {
        fetchDefault: function () {
            this.city = 'Bucharest'
            this.fetch(this.city)
        },
        fetch: function (city) {
            this.$http.get('/api/' + city).then(response => {
                console.log(response.body)
                this.model = response.body
            }, response => {
                // on err
            })
        }
    },
    watch: {
        'city': function(newVal, oldVal) {
            this.fetch(newVal)
        }
    },
    mounted: function() {
        this.fetchDefault()
    }
})