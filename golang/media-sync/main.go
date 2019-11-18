package main

import (
	"bufio"
	"encoding/json"
	"fmt"
	"io"
	"log"
	"net/http"
	"os"
	"strings"
)

const environment = "https://qa.scenedoc.com"

type TimelineEntry struct {
	ID          string `json:"id,omitempty"`
	Title       string `json:"title,omitempty"`
	EntryType   string `json:"entryType,omitempty"`
	StreamURL   string `json:"value,omitempty"`
	MediaStatus string `json:"mediaStatus,omitempty"`
	FileName    string `json:"fileName,omitempty"`
}

type TimelineEntries []TimelineEntry

func main() {
	fmt.Println("Welcome to SceneDoc Data Export")
	accessKeyID := getUserInput("Enter Access Key ID: ")
	token := getUserInput("Enter Token: ")
	fetchEntries(accessKeyID, token)
	fmt.Println("Data export complete, goodbye")
}

func getUserInput(inputText string) string {
	reader := bufio.NewReader(os.Stdin)
	fmt.Print(inputText)
	text, _ := reader.ReadString('\n')
	text = strings.TrimSuffix(text, "\n")
	return text
}

func fetchEntries(accessKeyID string, token string) {
	client := &http.Client{}

	// Request list of Timeline entries from SceneDoc
	endpoint := environment + "/rest/timelines"
	req, err := http.NewRequest("GET", endpoint, nil)
	req.SetBasicAuth(accessKeyID, token)
	req.Header.Set("Content-Type", "application/json")
	resp, err := client.Do(req)
	if err != nil {
		log.Fatal(err)
	}

	// Unmarshal response into struct
	timelines := TimelineEntries{}
	err = json.NewDecoder(resp.Body).Decode(&timelines)

	if err != nil {
		panic(err.Error)
	}

	// Loop through Timeline entry list and download binary if applicable
	fmt.Println("Retrieved", len(timelines), "Timeline Entries")
	for _, t := range timelines {
		if isValidEntryType(t.EntryType) && t.MediaStatus == "UPLOADED" && len(t.StreamURL) > 0 {
			fmt.Println("Downloading binary for entry:", t.Title)

			// create file in same directory as running binary
			out, err := os.Create(t.FileName)
			if err != nil {
				panic(err)
			}
			defer out.Close()

			// stream download to file
			req, err := http.NewRequest("GET", t.StreamURL, nil)
			req.SetBasicAuth(accessKeyID, token)
			resp, err := client.Do(req)
			if err != nil {
				panic(err)
			}
			defer resp.Body.Close()
			io.Copy(out, resp.Body)
		}
	}
}

// List of Timeline entry types that could have associated binary data
func isValidEntryType(entryType string) bool {
	switch entryType {
	case
		"PHOTO",
		"AUDIO",
		"SKETCHPAD",
		"VIDEO",
		"ATTACHMENT":
		return true
	}
	return false
}
